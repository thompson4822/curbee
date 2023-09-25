package com.example.stepDefinitions

import com.example.DiffTool
import com.example.models.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.jupiter.api.Assertions.*

class DiffToolStepDefinition {
    private val mapper = jacksonObjectMapper()

    private var objectStates: Map<String, RootState> = mapOf()

    private var previousState: RootState? = null
    private var currentState: RootState? = null
    private var difference: List<ChangeType> = listOf()
    private var createdException: Boolean = false

    private val sut = DiffTool()

    @Given("I have the following object states")
    fun `I have the following object states`(dataTable: DataTable) {
        val rows = dataTable.asMaps()
        val rawStates = rows
            .map { it["name"]!! to mapper.readValue(it["state"], RootState::class.java) }
            .toMap()

        val refinedStates = rawStates.map { (name, state) ->
            val list = state.c
            val newList = list.map {
                listItemReified(it)
            }
            name to state.copy(c = newList)
        }.toMap()

        objectStates = refinedStates
    }

    @Given("^I have a (.*) state and a (.*) state$")
    fun `I have a {previous} state and a {current} state`(previous: String, current: String) {
        previousState = if (previous == "null") null else objectStates[previous]
        currentState = if (current == "null") null else objectStates[current]
    }

    @When("I compare the two")
    fun `I compare the two`() {
        try {
            createdException = false
            difference = sut.diff(previousState, currentState)
        } catch (e: Exception) {
            createdException = true
        }
    }

    @Then("^I should see the expected (.*)?")
    fun `Then I should see the expected {differences}`(differences: String) {
        val dl = mapper.readValue(differences, List::class.java)
        val differenceList = dl.map {
                changeTypeReified(it!!)
        }
        assertTrue(difference.size == differenceList.size, "Difference size was not as expected")
        assertEquals(difference, differenceList, "Difference was not as expected")
    }

    @Then("I should see an exception")
    fun `I should see an exception`() {
        assertTrue(createdException, "No exception was created")
        assertTrue(difference.isEmpty(), "Difference was not empty")
    }

    @Then("I should not see an exception")
    fun `I should not see an exception`() {
        assertFalse(createdException, "An exception was created")
    }

    // This is to deal with the parser not knowing what type a particular list item is
    private fun listItemReified(it: Any) =
        if (it is LinkedHashMap<*, *>) {
            if (it.keys.contains("id")) {
                IdListObject(it["id"] as String, it["x"] as Int)
            } else if (it.contains("aid")) {
                AuditIdListObject(it["aid"] as String, it["x"] as Int)
            } else
                NoIdListObject(it["y"] as String, it["z"] as String)
        } else {
            it
        }

    // This is to deal with the parser not knowing what type a particular change type is
    private fun changeTypeReified(it: Any) =
        if (it is LinkedHashMap<*, *>) {
            if (it.keys.contains("added")) {
                ListUpdate(
                    it["property"] as String,
                    (it["added"] as List<Any>).map { listItemReified(it) },
                    (it["removed"] as List<Any>).map { listItemReified(it) },
                )
            } else {
                PropertyUpdate(it["property"] as String, it["previous"] as Any, it["current"] as Any)
            }
        } else {
            it
        }

}