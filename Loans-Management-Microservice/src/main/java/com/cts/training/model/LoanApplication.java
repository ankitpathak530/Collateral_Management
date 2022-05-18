package com.cts.training.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ApiModel(description = "Model class for loan application")
public class LoanApplication {

	@Id
	@ApiModelProperty(value = "Application Id of the Customer/Applicant")
	private Integer applicationId;
	
	@ApiModelProperty(value = "customer Id for the Application")
	private Integer customerId;
	
	@ApiModelProperty(value = "Loan amount of the application")
	private double loanAmount;
	
	@ApiModelProperty(value = "tenure of the Application")
	private Integer tenure;
	
	@ApiModelProperty(value = "collateral details of the Application")
	private String collateralDetails;
	
	@ApiModelProperty(value = "status of the Application")
	private String status;
	
}
