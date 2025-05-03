package com.example.demo.Controllers;


import com.example.demo.Domain.Branch;
import com.example.demo.Domain.Employer;
import com.example.demo.Domain.TypesAndEnums.Enums.Constants;
import com.example.demo.Domain.TypesAndEnums.Compositions.EmployerBranch;
import com.example.demo.Domain.Validators.Annotations.SafeString;
import com.example.demo.Domain.Views.BranchView;
import com.example.demo.Domain.Views.Directors.ViewDirector;
import com.example.demo.Domain.Views.EmployerView;
import com.example.demo.Exceptions.*;
import com.example.demo.Repositories.BranchRepo;
import com.example.demo.Repositories.EmployerRepo;
import com.example.demo.Services.BranchService;
import com.example.demo.Services.EmployerService;
import com.example.demo.Services.JobPostingService;
import com.example.demo.Services.Other.EmployerBranchPairService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


//TODO:Have seperate endpoints to get list of child entities of parent entities
//TODO:Get all companies for user, same for branches

@Tag(name = "Employer Management",description = "For CRUD operations of the employer and related entities")
@RestController
@Validated
@RequestMapping(path = "api/employer",produces = "application/json")
public class EmployerController {

    @Autowired
    private EmployerService employerService;
    @Autowired
    private BranchService branchService;
    @Autowired
    private EmployerBranchPairService ebpService;
    @Autowired
    private JobPostingService jobService;

    private static final String className=EmployerController.class.getName();
    private static final Logger logger= LoggerFactory.getLogger(className);
    @Autowired
    private BranchRepo branchRepo;
    @Autowired
    private EmployerRepo employerRepo;


    @PostMapping
    @Operation(summary = "Creates a new employer")
    @ResponseStatus(HttpStatus.CREATED)
    EmployerView createEmployer(@Valid @RequestBody Employer employer) throws ResourceAlreadyExistsException {
        EmployerView view = ViewDirector.getEmployerDetailedView(employerService.addEmployer(employer),new ArrayList<>());
        return view;
    }

    //TODO: Use Contants.DEFAULT_PAGE_SIZE as spEL in defaultValue
    @GetMapping(path = "/{employerName}")
    @Operation(summary = "Retrieves information about employer")
    EmployerView getEmployer(@PathVariable @SafeString String employerName) throws ResourceNotFoundException {

        Employer employer = employerService.getEmployerByName(employerName);
        EmployerView view = ViewDirector.getEmployerDetailedView(employer, new ArrayList<>());
        return view;
    }

    @Operation(summary = "Append employer information",
            description = "Privileged access required")
    @PatchMapping(path = "/{employerName}", consumes = "application/json-patch+json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEmployer(@PathVariable @SafeString String employerName,
                               @Valid @RequestBody JsonPatch patch) throws JsonPatchException, JsonProcessingException, InternalException {
        employerService.updateEmployer(employerName, patch);

    }

    @DeleteMapping(path = "/{employerName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployer(@PathVariable @SafeString String employerName) {
        try {
            employerService.deleteEmployer(employerName);
        } catch (ResourceNotFoundException e) {
            logger.warn("Invalid code path: Access control failed before searching for non existent employer");
        }

    }

    @Operation(summary = "Creates a new branch associated with the specified employer",
            description = "Privileged access required")
    @PostMapping(path = "/{employerName}/branch")
    @ResponseStatus(HttpStatus.CREATED)
    public BranchView addBranch(@PathVariable @SafeString String employerName,
                                @Valid @RequestBody Branch branch) throws ResourceAlreadyExistsException, InternalException, OperationConditionsFailedException {

        Branch savedBranch=branchService.createBranch(employerName,branch);
        BranchView view= ViewDirector.getBranchView(savedBranch,new ArrayList<>());

        return view;
    }

    //TODO: Use Contants.DEFAULT_PAGE_SIZE for default page size using spEL
    @Operation(summary = "Retrieves branch information")
    @GetMapping(path = "/{employerName}/branch")
    @ResponseStatus(HttpStatus.OK)
    public List<BranchView> getBranch(@PathVariable@SafeString String employerName,
                                      @RequestParam Optional<@SafeString String> branchName,
                                      @Parameter(schema = @Schema(defaultValue = "20")) @RequestParam Optional<@Min(1) Integer> pageSize,
                                      @RequestParam(defaultValue = "0") @Min(0) Integer pageNo) throws ResourceNotFoundException {
        Pageable pageable= PageRequest.of(pageNo,
                                            pageSize.orElse(Constants.DEFAULT_PAGE_SIZE.getIntValue()));

        List<Branch> branchList= branchService.getBranches(employerName,branchName,pageable);

        return branchList.stream().map(i->ViewDirector.getBranchView(i,new ArrayList<>())).collect(Collectors.toList());
    }

    @Operation(summary = "Appends branch information",
            description = "Privileged access required")
    @PatchMapping(path = "/{employerName}/branch",consumes = "application/json-patch+json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifyBranch(  @PathVariable @SafeString String employerName,
                               @RequestParam @SafeString String branchName,
                               @Valid @RequestBody JsonPatch patch) throws JsonPatchException, OperationConditionsFailedException, JsonProcessingException, ResourceNotFoundException, InternalException {
        EmployerBranch pair= ebpService.getPair(employerName,branchName);
        branchService.modifyBranch(pair ,patch);
    }

    @Operation(summary = "Deletes branch",
            description = "Privileged access required")
    @DeleteMapping(path = "/{employerName}/branch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBranch(@PathVariable @SafeString String employerName,
                             @RequestParam @SafeString String branchName,
                             @RequestParam(defaultValue = "false") boolean force) throws OperationConditionsFailedException, ResourceNotFoundException {
        EmployerBranch pair= ebpService.getPair(employerName,branchName);
        branchService.deleteBranch(pair,force);

    }

    @Operation(summary = "Upload employer profile picture",
            description = "Privileged access required")
    @PostMapping(path = "/{employerName}/profile-pic",consumes = {"image/jpeg","image/png","multipart/form-data"})
    @ResponseStatus(HttpStatus.OK)
    public UUID addProfilePic(@RequestParam MultipartFile file,
                              @PathVariable String employerName) throws HTTPException, InternalException {
         return employerService.uploadCompanyImage(employerName,file);
    }

//    @GetMapping(path = "/{employerName}/profile-pic")
//    public UUID getEmployerProfilePic(@PathVariable String employerName) throws RelationNotFoundException {
//        return employerService.getProfilePicIdentifier(employerName);
//
//    }

}
