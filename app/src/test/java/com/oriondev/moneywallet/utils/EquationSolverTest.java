package com.oriondev.moneywallet.utils;

import com.oriondev.moneywallet.model.CurrencyUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class EquationSolverTest {

    @InjectMocks
    private EquationSolver equationSolver;

    private void mockCurrencyUnit(int decimals) {
        equationSolver.mCurrency = new CurrencyUnit("", "", "", decimals);
    }

    /*
    Order of tests:
    For each supported currency based on decimal count:
    - Test zero value input
    - Test positive value input with expected number of decimals
    - Test negative value input with expected number of decimals
    - Test positive value input with the last decimal missing (except for zero decimal currency)
    - Test negative value input with the last decimal missing (except for zero decimal currency)
    - Test positive value input with an extra decimal
    - Test negative value input with an extra decimal
     */

    // Zero Decimal Currency

    @Test
    public void testGetResult_zeroDecimalCurrencyAndZeroValue() {
        mockCurrencyUnit(0);
        equationSolver.mFirstNumber = "0";

        long result = equationSolver.getResult();

        assertEquals(result, 0L);
    }

    @Test
    public void testGetResult_zeroDecimalCurrencyAndPositiveValue() {
        mockCurrencyUnit(0);
        equationSolver.mFirstNumber = "64";

        long result = equationSolver.getResult();

        assertEquals(result, 64L);
    }

    @Test
    public void testGetResult_zeroDecimalCurrencyAndNegativeValue() {
        mockCurrencyUnit(0);
        equationSolver.mFirstNumber = "-64";

        long result = equationSolver.getResult();

        assertEquals(result, -64L);
    }

    @Test
    public void testGetResult_zeroDecimalCurrencyAndPositiveValueWithExtraDecimal() {
        mockCurrencyUnit(0);
        equationSolver.mFirstNumber = "64.9";

        long result = equationSolver.getResult();

        assertEquals(result, 64L);
    }

    @Test
    public void testGetResult_zeroDecimalCurrencyAndNegativeValueWithExtraDecimal() {
        mockCurrencyUnit(0);
        equationSolver.mFirstNumber = "-64.9";

        long result = equationSolver.getResult();

        assertEquals(result, -64L);
    }

    // One Decimal Currency

    @Test
    public void testGetResult_oneDecimalCurrencyAndZeroValue() {
        mockCurrencyUnit(1);
        equationSolver.mFirstNumber = "0";

        long result = equationSolver.getResult();

        assertEquals(result, 0L);
    }

    @Test
    public void testGetResult_oneDecimalCurrencyAndPositiveValue() {
        mockCurrencyUnit(1);
        equationSolver.mFirstNumber = "64.9";

        long result = equationSolver.getResult();

        assertEquals(result, 649L);
    }

    @Test
    public void testGetResult_oneDecimalCurrencyAndNegativeValue() {
        mockCurrencyUnit(1);
        equationSolver.mFirstNumber = "-64.9";

        long result = equationSolver.getResult();

        assertEquals(result, -649L);
    }

    @Test
    public void testGetResult_oneDecimalCurrencyAndPositiveValueWithMissingLastDecimal() {
        mockCurrencyUnit(1);
        equationSolver.mFirstNumber = "64";

        long result = equationSolver.getResult();

        assertEquals(result, 640L);
    }

    @Test
    public void testGetResult_oneDecimalCurrencyAndNegativeValueWithMissingLastDecimal() {
        mockCurrencyUnit(1);
        equationSolver.mFirstNumber = "-64";

        long result = equationSolver.getResult();

        assertEquals(result, -640L);
    }

    @Test
    public void testGetResult_oneDecimalCurrencyAndPositiveValueWithExtraDecimal() {
        mockCurrencyUnit(1);
        equationSolver.mFirstNumber = "64.99";

        long result = equationSolver.getResult();

        assertEquals(result, 649L);
    }

    @Test
    public void testGetResult_oneDecimalCurrencyAndNegativeValueWithExtraDecimal() {
        mockCurrencyUnit(1);
        equationSolver.mFirstNumber = "-64.99";

        long result = equationSolver.getResult();

        assertEquals(result, -649L);
    }

    // Two Decimal Currency

    @Test
    public void testGetResult_towDecimalCurrencyAndZeroValue() {
        mockCurrencyUnit(2);
        equationSolver.mFirstNumber = "0";

        long result = equationSolver.getResult();

        assertEquals(result, 0L);
    }

    @Test
    public void testGetResult_towDecimalCurrencyAndPositiveValue() {
        mockCurrencyUnit(2);
        equationSolver.mFirstNumber = "64.99";

        long result = equationSolver.getResult();

        assertEquals(result, 6499L);
    }

    @Test
    public void testGetResult_towDecimalCurrencyAndNegativeValue() {
        mockCurrencyUnit(2);
        equationSolver.mFirstNumber = "-64.99";

        long result = equationSolver.getResult();

        assertEquals(result, -6499L);
    }

    @Test
    public void testGetResult_towDecimalCurrencyAndPositiveValueWithMissingLastDecimal() {
        mockCurrencyUnit(2);
        equationSolver.mFirstNumber = "64.9";

        long result = equationSolver.getResult();

        assertEquals(result, 6490L);
    }

    @Test
    public void testGetResult_towDecimalCurrencyAndNegativeValueWithMissingLastDecimal() {
        mockCurrencyUnit(2);
        equationSolver.mFirstNumber = "-64.9";

        long result = equationSolver.getResult();

        assertEquals(result, -6490L);
    }

    @Test
    public void testGetResult_towDecimalCurrencyAndPositiveValueWithExtraDecimal() {
        mockCurrencyUnit(2);
        equationSolver.mFirstNumber = "64.999";

        long result = equationSolver.getResult();

        assertEquals(result, 6499L);
    }

    @Test
    public void testGetResult_towDecimalCurrencyAndNegativeValueWithExtraDecimal() {
        mockCurrencyUnit(2);
        equationSolver.mFirstNumber = "-64.999";

        long result = equationSolver.getResult();

        assertEquals(result, -6499L);
    }


    // Three Decimal Currency

    @Test
    public void testGetResult_threeDecimalCurrencyAndZeroValue() {
        mockCurrencyUnit(3);
        equationSolver.mFirstNumber = "0";

        long result = equationSolver.getResult();

        assertEquals(result, 0L);
    }

    @Test
    public void testGetResult_threeDecimalCurrencyAndPositiveValue() {
        mockCurrencyUnit(3);
        equationSolver.mFirstNumber = "64.999";

        long result = equationSolver.getResult();

        assertEquals(result, 64999L);
    }

    @Test
    public void testGetResult_threeDecimalCurrencyAndNegativeValue() {
        mockCurrencyUnit(3);
        equationSolver.mFirstNumber = "-64.999";

        long result = equationSolver.getResult();

        assertEquals(result, -64999L);
    }

    @Test
    public void testGetResult_threeDecimalCurrencyAndPositiveValueWithMissingLastDecimal() {
        mockCurrencyUnit(3);
        equationSolver.mFirstNumber = "64.99";

        long result = equationSolver.getResult();

        assertEquals(result, 64990L);
    }

    @Test
    public void testGetResult_threeDecimalCurrencyAndNegativeValueWithMissingLastDecimal() {
        mockCurrencyUnit(3);
        equationSolver.mFirstNumber = "-64.99";

        long result = equationSolver.getResult();

        assertEquals(result, -64990L);
    }

    @Test
    public void testGetResult_threeDecimalCurrencyAndPositiveValueWithExtraDecimal() {
        mockCurrencyUnit(3);
        equationSolver.mFirstNumber = "64.9999";

        long result = equationSolver.getResult();

        assertEquals(result, 64999L);
    }

    @Test
    public void testGetResult_threeDecimalCurrencyAndNegativeValueWithExtraDecimal() {
        mockCurrencyUnit(3);
        equationSolver.mFirstNumber = "-64.9999";

        long result = equationSolver.getResult();

        assertEquals(result, -64999L);
    }
}