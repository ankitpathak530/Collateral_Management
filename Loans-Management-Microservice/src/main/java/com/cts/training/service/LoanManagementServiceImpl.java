package com.cts.training.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import com.cts.training.exception.CollateralTypeNotFoundException;
import com.cts.training.exception.CustomerLoanNotFoundException;
import com.cts.training.exception.LoanNotFoundException;
import com.cts.training.feign.CollateralFeign;
import com.cts.training.model.CustomerLoan;
import com.cts.training.model.Loan;
import com.cts.training.model.LoanApplication;
import com.cts.training.pojo.CashDeposit;
import com.cts.training.pojo.RealEstate;
import com.cts.training.repo.CustomerLoanRepo;
import com.cts.training.repo.LoanApplicationRepository;
import com.cts.training.repo.LoanRepo;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

/**
 * LoanManagementService implementation
 */
@Service
@Slf4j
public class LoanManagementServiceImpl implements LoanManagementService {

	@Autowired
	private CollateralFeign client;

	@Autowired
	private CustomerLoanRepo customerLoanRepo;

	@Autowired
	private LoanRepo loanRepo;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;
	
	
	private static final String MESSAGE = "Customer Loan Not found with LoanId: ";

	
	/**
	 * Get Loan Details Implimentation
	 */
	@Override
	public CustomerLoan getLoanDetails(int loanId, int customerId) throws CustomerLoanNotFoundException {
		log.info("Get Loan details using loan id and customer id");
		log.info(loanId+"======="+customerId);
		System.out.println("Inside loan management service================");
		CustomerLoan customerLoan = customerLoanRepo.findById(loanId)
		.orElseThrow(() -> new CustomerLoanNotFoundException(MESSAGE + loanId));
		/*
		 * Optional<CustomerLoan> customerLoan=customerLoanRepo.findById(loanId);
		 * System.out.println(customerLoan.get()); if(!customerLoan.isPresent()) { throw
		 * new CustomerLoanNotFoundException(MESSAGE+loanId); }
		 */
		System.out.println(customerLoan);
		if (customerLoan.getCustomerId() != customerId) {
			throw new CustomerLoanNotFoundException(MESSAGE + loanId);
		}
		return customerLoan;
	}
	
	/**
	 * Save RealEstate Implementatiom
	 * 
	 * @throws LoanNotFoundException
	 */
	@Override
	public ResponseEntity<String> saveRealEstate(String token, RealEstate realEstate)
			throws CustomerLoanNotFoundException, LoanNotFoundException {
		log.info("Save Real Estate collateral details");
		System.out.println("===========Saving Real Estate details============= from loan management service"+realEstate);
		CustomerLoan customerLoan = customerLoanRepo.findById(realEstate.getLoanId())
				.orElseThrow(() -> new CustomerLoanNotFoundException(MESSAGE + realEstate.getLoanId()));

		Integer prodId = customerLoan.getLoanProductId();
		Optional<Loan> loanop = loanRepo.findById(prodId);
		if(!loanop.isPresent()){
			throw new LoanNotFoundException("Loan Not found by Id" + prodId);
		}else{
			Loan loan = loanop.get();
			String type = loan.getCollateralType();
		try {
			if (type.equals("REAL_ESTATE")) {

				customerLoan.setCollateralId(realEstate.getCollateralId());
				customerLoanRepo.save(customerLoan);
				return client.saveRealEstateCollateral(token, realEstate);
			} else {
				throw new CollateralTypeNotFoundException("Collateral Mismatch");
			}
		} catch (FeignException e) {
			e.printStackTrace();
			throw new CollateralTypeNotFoundException("Collateral already exists with loan id");
		}
		}
	}
	
	/**
	 * Save Cash Deposit Implementation
	 * 
	 * @throws LoanNotFoundException
	 */
	@Override
	public ResponseEntity<String> saveCashDeposit(String token, CashDeposit cashDeposit)
			throws CustomerLoanNotFoundException, LoanNotFoundException {
		log.info("Save Cash Deposit collateral details");
		CustomerLoan customerLoan = customerLoanRepo.findById(cashDeposit.getLoanId())
				.orElseThrow(() -> new CustomerLoanNotFoundException(MESSAGE + cashDeposit.getLoanId()));

		Integer prodId = customerLoan.getLoanProductId();
		Optional<Loan> loanop = loanRepo.findById(prodId);
		if(!loanop.isPresent()){
			throw new LoanNotFoundException("Loan not Found By Id:" + prodId);
		}else{
			Loan loan = loanop.get();
			String type = loan.getCollateralType();
			try {
				if (type.equals("CASH_DEPOSIT")) {
					customerLoan.setCollateralId(cashDeposit.getCollateralId());
					customerLoanRepo.save(customerLoan);
					return client.saveCashDepositCollateral(token, cashDeposit);
				} else {
					throw new CollateralTypeNotFoundException("Collateral Mismatch");
				}
			} catch (FeignException e) {
				
				throw new CollateralTypeNotFoundException("Collateral already exists with loan id");
			}
		}
	}

	
	
	
	/********AP**********/
	
	

	
	@Override
	public String applyLoan(LoanApplication loan_application) {
		this.loanApplicationRepository.save(loan_application);
		return "Saved";
	}

	@Override
	public Optional<LoanApplication> getLoanApplicationStatus(Integer application_id) {
		return this.loanApplicationRepository.findById(application_id);
	}

	@Override
	public String approveLoanApplication(Integer application_id) {
		Optional<LoanApplication> findById = this.loanApplicationRepository.findById(application_id);
	    if(!findById.isEmpty()) {
	        LoanApplication loanApplication = findById.get();
	 	    loanApplication.setStatus("Approved");
	 	    this.loanApplicationRepository.save(loanApplication);
	 	  
	 	    
			CustomerLoan customerLoan=new CustomerLoan();
			Integer collateralId=0;
			if(loanApplication.getCollateralDetails().equalsIgnoreCase("Cash Deposit")) {
				collateralId=101;
			}
			else if(loanApplication.getCollateralDetails().equalsIgnoreCase("Real Estate")) {
				collateralId=102;
			}
			
			
			Double emi=(Double)loanApplication.getLoanAmount()/12.0*loanApplication.getTenure();
			customerLoan.setCustomerId(loanApplication.getCustomerId());
			customerLoan.setLoanPrincipal(loanApplication.getLoanAmount());
			customerLoan.setTenure(loanApplication.getTenure());
			customerLoan.setInterest(10.5);
			customerLoan.setEmi(emi);
			customerLoan.setCollateralId(collateralId);
			
			customerLoanRepo.save(customerLoan); 
			customerLoan.setLoanProductId(customerLoan.getLoanId()+1000);
			customerLoanRepo.save(customerLoan);
			 
	 	    return "Approved";
	    }
	   return null;
	}

	
	@Override
	public String rejectLoanApplication(Integer application_id) {
		Optional<LoanApplication> findById = this.loanApplicationRepository.findById(application_id);
	    if(!findById.isEmpty()) {
	        LoanApplication loanApplication = findById.get();
	 	    loanApplication.setStatus("Rejected");
	 	    this.loanApplicationRepository.save(loanApplication);
	 	    return "Rejected";
	    }
	    return null;
	}
	
	
	
	
	
	
	
	
}
