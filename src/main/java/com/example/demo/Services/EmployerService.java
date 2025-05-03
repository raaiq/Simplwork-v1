package com.example.demo.Services;

import com.example.demo.Domain.*;
import com.example.demo.Authentication.UserInfoInterface;
import com.example.demo.Domain.RolesAndAuthorities.EmployerRole;
import com.example.demo.Domain.TypesAndEnums.Compositions.EmployerBranch;
import com.example.demo.Exceptions.*;
import com.example.demo.Repositories.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

//TODO:Have way to update user repo and security context simultaneously
@Service
public class EmployerService {

    @Autowired
    private EmployerRepo employerRepo;
    @Autowired
    private EmployerUserRepo employerUserRepo;

    @Autowired
    private BranchRepo branchRepo;

    @Autowired
    private BranchService branchService;

    @Autowired
    private EmployerImageFileRepo employerImageFileRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private Validator validator;
    @Autowired
    private MatcherService matcherService;

    @Value("${custom.employer.images.dir}")
    private String employerImageDir;


    @Autowired
    private ObjectMapper objectMapper;
    private static final String className=EmployerService.class.getName();
    private static final Logger logger= LoggerFactory.getLogger(className);




    @PostConstruct
    // TODO:Might be redundant to check if value is null
    void postInitialization(){
        if(employerImageDir == null){
            logger.error("Environment variable EMPLOYER_IMAGE_DIR not set");
            System.exit(-1);
        }
    }

    //Modify to enable only paying users
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public Employer addEmployer(Employer employer) throws ResourceAlreadyExistsException {
        if(employerRepo.existsByCompanyNameAllIgnoreCase(employer.getCompanyName())){
            throw new ResourceAlreadyExistsException("Employer name with name "+employer.getCompanyName()+" already exits");
        }
        UserInfo owner=((UserInfoInterface)SecurityContextHolder.getContext().getAuthentication()).getUserInfo();
        employer.initializeNewEntity();
        employer=employerRepo.save(employer);
        EmployerUser empUser=new EmployerUser();
        empUser.setEmployer(employer);
        empUser.setUserInfo(owner);
        empUser.addRoles(EmployerRole.OWNER);
        employerUserRepo.save(empUser);
        owner.addEmployerUserProfile(empUser);
        userRepo.save(owner);
        return employer;
    }

    Employer getEmployerByID(long employerID) throws ResourceNotFoundException {
        Optional<Employer> e=employerRepo.findById(employerID);
        if(e.isEmpty()){
            throw new ResourceNotFoundException("Couldn't find employer by id"+employerID);
        }
        return e.get();
    }

    //Returns all data available for employer:branches,postings can optimize
    @PreAuthorize("hasRole('USER')")
    public Employer getEmployerByName(String employerName) throws ResourceNotFoundException {
        Optional<Employer> entry=employerRepo.findByCompanyNameIgnoreCase(employerName);
        if(entry.isPresent()){
            return entry.get();
        }
        throw new ResourceNotFoundException("Couldn't find employer with name: "+employerName);

    }

    //TODO:Pass employer object instead of employerName
    @PreAuthorize("hasPermission(#employerName,T(com.example.demo.Domain.RolesAndAuthorities.EmployerAuthority).MODIFY_INFO)")
    @Transactional
    public void updateEmployer(String employerName,JsonPatch patch) throws JsonPatchException, JsonProcessingException, InternalException {

        final String methodName="updateEmployer";
        Employer employer= null;
        try {
            employer = ((UserInfoInterface) SecurityContextHolder.getContext().getAuthentication()).getUserInfo().findEmployerUser(employerName).getEmployer();
        } catch (ResourceNotFoundException e) {
            throw new InternalException("Invalid code path: Employer not associated with user");
        }
        JsonNode node = patch.apply(objectMapper.convertValue(employer,JsonNode.class));
        Employer patchedEmployer = objectMapper.treeToValue(node, Employer.class);
        patchedEmployer.copyFixedTraitsAndSanitizeFields(employer);
        Set<ConstraintViolation<Employer>> errors =validator.validate(patchedEmployer);
        if(!errors.isEmpty()){
            throw new ConstraintViolationException(errors);
        }
        employerRepo.save(patchedEmployer);
    }

    //TODO: Have better implementation
    //TODO: Check if transactional annotation is needed
    @PreAuthorize("hasPermission(#employerName, T(com.example.demo.Domain.RolesAndAuthorities.EmployerAuthority).DELETE_EMPLOYER)")
    @Transactional
    public void deleteEmployer(String employerName) throws ResourceNotFoundException {

        Employer employer=employerRepo.findByCompanyNameIgnoreCase(employerName).orElseThrow(()->new ResourceNotFoundException("Employer doesn't exist"));
        List<Branch> branches;
        Pageable pageable= PageRequest.of(0,100);
        branches= branchRepo.findByCompany_CompanyName(employerName,pageable);

        do{
            branches.forEach(i-> {
                try {
                    branchService.deleteBranch(new EmployerBranch(employer,i),true);
                } catch (OperationConditionsFailedException ignored) {}
            });
            branches= branchRepo.findByCompany_CompanyName(employerName,pageable);

        }while (!branches.isEmpty());

        employerUserRepo.deleteByEmployer(employer);
        employerRepo.delete(employer);


    }

    //TODO:Properly sanitize file, and check file format. Preferably do file handling in a separate containerized application
    @PreAuthorize("hasPermission(#employerName, T(com.example.demo.Domain.RolesAndAuthorities.EmployerAuthority).MODIFY_INFO)")
    @Transactional
    public UUID uploadCompanyImage(String employerName, MultipartFile file) throws HTTPException, InternalException {

        Tika tika = new Tika();
        String mimeType="";
        try {
            mimeType = tika.detect(file.getInputStream());
        }catch (IOException e){
            logger.error("Error processing uploaded file",e);
        }


        EmployerImageFile fileLocation= new EmployerImageFile();

        boolean isPNG = false;
        if(!((isPNG=mimeType.equals("image/png")) || mimeType.equals("image/jpeg"))){
            logger.warn("Attempt to upload file of invalid type: "+mimeType);
            throw new HTTPException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        String fileName= employerName;
        fileName+= isPNG ? ".png" :".jpeg";
        fileLocation.setFileName(fileName);
        fileLocation.setPNG(isPNG);

        Employer employer= employerRepo.findByCompanyNameIgnoreCase(employerName).get();
        if(employer.getCompanyImage() != null){
            File existingImage=new File(employerImageDir+"/"+employer.getCompanyImage().getFileName());
            if(!existingImage.delete()){
                throw new InternalException("Unable to delete existing image file: "+ existingImage.getPath()+" for employer: "+employerName);
            }
        }

        File image=new File(employerImageDir+"/"+fileName);

        try {
            file.transferTo(image);
        } catch (IOException e) {
            throw new InternalException("Error saving file",e);
        }

        fileLocation=employerImageFileRepo.save(fileLocation);

        employer.setCompanyImage(fileLocation);

        employerRepo.save(employer);

        return fileLocation.getImageID();
    }

    public UUID getProfilePicIdentifier(String employerName) throws ResourceNotFoundException {
        return employerRepo.findByCompanyNameIgnoreCase(employerName)
                .orElseThrow(()->new ResourceNotFoundException("Employer "+ employerName+ " not found"))
                .getCompanyImage().getImageID();
    }


}
