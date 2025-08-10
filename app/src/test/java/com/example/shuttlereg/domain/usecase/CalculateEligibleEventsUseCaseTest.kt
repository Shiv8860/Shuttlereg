package com.example.shuttlereg.domain.usecase

import com.example.shuttlereg.domain.model.EventCategory
import com.example.shuttlereg.domain.model.Gender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CalculateEligibleEventsUseCaseTest {

    private lateinit var useCase: CalculateEligibleEventsUseCase

    @Before
    fun setUp() {
        useCase = CalculateEligibleEventsUseCase()
    }

    @Test
    fun `invoke with 2020 birth year should return U9 category only`() {
        // Given
        val dateOfBirth = LocalDate.of(2020, 6, 15)
        val gender = Gender.MALE

        // When
        val result = useCase.invoke(dateOfBirth, gender)

        // Then
        assertEquals(1, result.size)
        assertTrue(result.contains(EventCategory.U9))
    }

    @Test
    fun `invoke with 2016 birth year should return U9 category`() {
        // Given
        val dateOfBirth = LocalDate.of(2016, 1, 1)
        val gender = Gender.FEMALE

        // When
        val result = useCase.invoke(dateOfBirth, gender)

        // Then
        assertEquals(1, result.size)
        assertTrue(result.contains(EventCategory.U9))
    }

    @Test
    fun `invoke with 2014 birth year should return U9 and U11 categories`() {
        // Given
        val dateOfBirth = LocalDate.of(2014, 6, 15)
        val gender = Gender.MALE

        // When
        val result = useCase.invoke(dateOfBirth, gender)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.contains(EventCategory.U9))
        assertTrue(result.contains(EventCategory.U11))
    }

    @Test
    fun `invoke with 2012 birth year should return U9, U11, and U13 categories`() {
        // Given
        val dateOfBirth = LocalDate.of(2012, 6, 15)
        val gender = Gender.FEMALE

        // When
        val result = useCase.invoke(dateOfBirth, gender)

        // Then
        assertEquals(3, result.size)
        assertTrue(result.contains(EventCategory.U9))
        assertTrue(result.contains(EventCategory.U11))
        assertTrue(result.contains(EventCategory.U13))
    }

    @Test
    fun `invoke with 2010 birth year should return all youth categories`() {
        // Given
        val dateOfBirth = LocalDate.of(2010, 6, 15)
        val gender = Gender.MALE

        // When
        val result = useCase.invoke(dateOfBirth, gender)

        // Then
        assertEquals(4, result.size)
        assertTrue(result.contains(EventCategory.U9))
        assertTrue(result.contains(EventCategory.U11))
        assertTrue(result.contains(EventCategory.U13))
        assertTrue(result.contains(EventCategory.U15))
    }

    @Test
    fun `invoke with 2008 birth year should return all youth categories including U17`() {
        // Given
        val dateOfBirth = LocalDate.of(2008, 6, 15)
        val gender = Gender.FEMALE

        // When
        val result = useCase.invoke(dateOfBirth, gender)

        // Then
        assertEquals(5, result.size)
        assertTrue(result.contains(EventCategory.U9))
        assertTrue(result.contains(EventCategory.U11))
        assertTrue(result.contains(EventCategory.U13))
        assertTrue(result.contains(EventCategory.U15))
        assertTrue(result.contains(EventCategory.U17))
    }

    @Test
    fun `invoke with 2006 birth year should return all youth categories including U19`() {
        // Given
        val dateOfBirth = LocalDate.of(2006, 6, 15)
        val gender = Gender.MALE

        // When
        val result = useCase.invoke(dateOfBirth, gender)

        // Then
        assertEquals(6, result.size)
        assertTrue(result.contains(EventCategory.U9))
        assertTrue(result.contains(EventCategory.U11))
        assertTrue(result.contains(EventCategory.U13))
        assertTrue(result.contains(EventCategory.U15))
        assertTrue(result.contains(EventCategory.U17))
        assertTrue(result.contains(EventCategory.U19))
    }

    @Test
    fun `invoke with 2005 birth year and male gender should include mens open`() {
        // Given
        val dateOfBirth = LocalDate.of(2005, 6, 15)
        val gender = Gender.MALE

        // When
        val result = useCase.invoke(dateOfBirth, gender)

        // Then
        assertEquals(7, result.size)
        assertTrue(result.contains(EventCategory.U9))
        assertTrue(result.contains(EventCategory.U11))
        assertTrue(result.contains(EventCategory.U13))
        assertTrue(result.contains(EventCategory.U15))
        assertTrue(result.contains(EventCategory.U17))
        assertTrue(result.contains(EventCategory.U19))
        assertTrue(result.contains(EventCategory.MENS_OPEN))
    }

    @Test
    fun `invoke with 2005 birth year and female gender should include womens open`() {
        // Given
        val dateOfBirth = LocalDate.of(2005, 6, 15)
        val gender = Gender.FEMALE

        // When
        val result = useCase.invoke(dateOfBirth, gender)

        // Then
        assertEquals(7, result.size)
        assertTrue(result.contains(EventCategory.U9))
        assertTrue(result.contains(EventCategory.U11))
        assertTrue(result.contains(EventCategory.U13))
        assertTrue(result.contains(EventCategory.U15))
        assertTrue(result.contains(EventCategory.U17))
        assertTrue(result.contains(EventCategory.U19))
        assertTrue(result.contains(EventCategory.WOMENS_OPEN))
    }

    @Test
    fun `invoke with 1990 birth year and male gender should include all categories with mens open`() {
        // Given
        val dateOfBirth = LocalDate.of(1990, 6, 15)
        val gender = Gender.MALE

        // When
        val result = useCase.invoke(dateOfBirth, gender)

        // Then
        assertEquals(7, result.size)
        assertTrue(result.contains(EventCategory.MENS_OPEN))
    }

    @Test
    fun `invoke with 1990 birth year and female gender should include all categories with womens open`() {
        // Given
        val dateOfBirth = LocalDate.of(1990, 6, 15)
        val gender = Gender.FEMALE

        // When
        val result = useCase.invoke(dateOfBirth, gender)

        // Then
        assertEquals(7, result.size)
        assertTrue(result.contains(EventCategory.WOMENS_OPEN))
    }

    @Test
    fun `getAgeFromDateOfBirth should calculate correct age`() {
        // Given
        val dateOfBirth = LocalDate.of(2010, 6, 15)

        // When
        val age = useCase.getAgeFromDateOfBirth(dateOfBirth)

        // Then
        val expectedAge = LocalDate.now().year - 2010
        assertTrue(age == expectedAge || age == expectedAge - 1) // Account for birthday not yet passed
    }

    @Test
    fun `isEligibleForCategory should return true for valid U13 participant`() {
        // Given
        val dateOfBirth = LocalDate.of(2012, 6, 15)
        val category = EventCategory.U13

        // When
        val result = useCase.isEligibleForCategory(dateOfBirth, category)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isEligibleForCategory should return false for invalid U13 participant`() {
        // Given
        val dateOfBirth = LocalDate.of(2011, 6, 15) // Too young for U13
        val category = EventCategory.U13

        // When
        val result = useCase.isEligibleForCategory(dateOfBirth, category)

        // Then
        assertTrue(!result)
    }

    @Test
    fun `isEligibleForCategory should return true for valid mens open participant`() {
        // Given
        val dateOfBirth = LocalDate.of(2005, 6, 15)
        val category = EventCategory.MENS_OPEN

        // When
        val result = useCase.isEligibleForCategory(dateOfBirth, category)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isEligibleForCategory should return false for invalid mens open participant`() {
        // Given
        val dateOfBirth = LocalDate.of(2007, 6, 15) // Too young for mens open
        val category = EventCategory.MENS_OPEN

        // When
        val result = useCase.isEligibleForCategory(dateOfBirth, category)

        // Then
        assertTrue(!result)
    }
}