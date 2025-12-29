package com.logifin.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for financial calculations including interest, profit, and fees
 */
public class FinancialCalculationUtil {

    private static final int DAYS_IN_YEAR = 365;
    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Calculate simple interest based on principal, annual interest rate, and days
     * Formula: Interest = (Principal × Rate × Days) / (100 × 365)
     *
     * @param principal Principal amount
     * @param annualInterestRate Annual interest rate as percentage (e.g., 12.5 for 12.5%)
     * @param days Number of days
     * @return Calculated interest amount
     */
    public static BigDecimal calculateSimpleInterest(BigDecimal principal, BigDecimal annualInterestRate, long days) {
        if (principal == null || annualInterestRate == null || days <= 0) {
            return BigDecimal.ZERO;
        }

        // Interest = (Principal × Rate × Days) / (100 × 365)
        BigDecimal interest = principal
                .multiply(annualInterestRate)
                .multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(100 * DAYS_IN_YEAR), SCALE, ROUNDING_MODE);

        return interest.setScale(2, ROUNDING_MODE);
    }

    /**
     * Calculate interest between two dates
     *
     * @param principal Principal amount
     * @param annualInterestRate Annual interest rate as percentage
     * @param startDate Start date
     * @param endDate End date
     * @return Calculated interest amount
     */
    public static BigDecimal calculateInterestBetweenDates(BigDecimal principal, BigDecimal annualInterestRate,
                                                           LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return BigDecimal.ZERO;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return calculateSimpleInterest(principal, annualInterestRate, days);
    }

    /**
     * Calculate total repayment amount (principal + interest)
     *
     * @param principal Principal amount
     * @param annualInterestRate Annual interest rate as percentage
     * @param days Number of days
     * @return Total repayment amount
     */
    public static BigDecimal calculateTotalRepayment(BigDecimal principal, BigDecimal annualInterestRate, long days) {
        BigDecimal interest = calculateSimpleInterest(principal, annualInterestRate, days);
        return principal.add(interest).setScale(2, ROUNDING_MODE);
    }

    /**
     * Calculate percentage of an amount
     *
     * @param amount Base amount
     * @param percentage Percentage (e.g., 0.5 for 0.5%)
     * @return Calculated percentage amount
     */
    public static BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {
        if (amount == null || percentage == null) {
            return BigDecimal.ZERO;
        }

        return amount
                .multiply(percentage)
                .divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE)
                .setScale(2, ROUNDING_MODE);
    }

    /**
     * Calculate portal service charge
     *
     * @param amount Transaction amount
     * @param serviceChargePercentage Service charge percentage (e.g., 0.5 for 0.5%)
     * @return Service charge amount
     */
    public static BigDecimal calculatePortalServiceCharge(BigDecimal amount, BigDecimal serviceChargePercentage) {
        return calculatePercentage(amount, serviceChargePercentage);
    }

    /**
     * Calculate net amount after deducting portal service charge
     *
     * @param amount Transaction amount
     * @param serviceChargePercentage Service charge percentage
     * @return Net amount after deduction
     */
    public static BigDecimal calculateNetAmountAfterServiceCharge(BigDecimal amount, BigDecimal serviceChargePercentage) {
        BigDecimal serviceCharge = calculatePortalServiceCharge(amount, serviceChargePercentage);
        return amount.subtract(serviceCharge).setScale(2, ROUNDING_MODE);
    }

    /**
     * Calculate lender's profit
     * Profit = Total Interest Received - Portal Fee on Interest
     *
     * @param principalAmount Principal amount lent
     * @param totalRepaymentReceived Total amount received back
     * @param portalFeeOnTransaction Portal fee charged on transaction
     * @return Lender's net profit
     */
    public static BigDecimal calculateLenderProfit(BigDecimal principalAmount,
                                                    BigDecimal totalRepaymentReceived,
                                                    BigDecimal portalFeeOnTransaction) {
        if (principalAmount == null || totalRepaymentReceived == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal grossProfit = totalRepaymentReceived.subtract(principalAmount);
        BigDecimal netProfit = grossProfit.subtract(portalFeeOnTransaction != null ? portalFeeOnTransaction : BigDecimal.ZERO);
        return netProfit.setScale(2, ROUNDING_MODE);
    }

    /**
     * Calculate transporter's net profit
     * Profit = (Amount Paid by Shipper) - (Amount Repaid to Lender including interest)
     *
     * @param shipperPayment Amount paid by shipper for the trip
     * @param lenderRepayment Amount repaid to lender (principal + interest)
     * @return Transporter's profit
     */
    public static BigDecimal calculateTransporterProfit(BigDecimal shipperPayment, BigDecimal lenderRepayment) {
        if (shipperPayment == null || lenderRepayment == null) {
            return BigDecimal.ZERO;
        }

        return shipperPayment.subtract(lenderRepayment).setScale(2, ROUNDING_MODE);
    }

    /**
     * Calculate days between two dates
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Number of days
     */
    public static long calculateDaysBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculate effective annual rate from a given rate and days
     *
     * @param totalReturn Total return amount
     * @param principal Principal amount
     * @param days Number of days
     * @return Effective annual rate as percentage
     */
    public static BigDecimal calculateEffectiveAnnualRate(BigDecimal totalReturn, BigDecimal principal, long days) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) == 0 || days <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal profit = totalReturn.subtract(principal);
        // Rate = (Profit / Principal) × (365 / Days) × 100
        return profit
                .divide(principal, SCALE, ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(DAYS_IN_YEAR))
                .divide(BigDecimal.valueOf(days), SCALE, ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, ROUNDING_MODE);
    }
}
