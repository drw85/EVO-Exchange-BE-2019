package com.hillel.items_exchange.controller;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import com.hillel.items_exchange.dto.CategoryDto;
import com.hillel.items_exchange.exception.InvalidDtoException;
import com.hillel.items_exchange.util.CategoryControllerIntegrationTestUtil;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import static com.hillel.items_exchange.util.JsonConverter.asJsonString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DBRider
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:index-reset.sql")
public class CategoryControllerIntegrationTest extends CategoryControllerIntegrationTestUtil {

    private final MockMvc mockMvc;

    @Autowired
    public CategoryControllerIntegrationTest(MockMvc mockMvc) {
        super();
        this.mockMvc = mockMvc;
    }

    @Test
    @Transactional
    @DataSet("database_init.yml")
    void getAllCategoriesNames_shouldReturnAllCategoriesNames() throws Exception {
        mockMvc.perform(get("/category/names")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @DataSet("database_init.yml")
    void getAllCategories_shouldReturnAllCategories() throws Exception {
        mockMvc.perform(get("/category/all")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @DataSet("database_init.yml")
    void getCategoryById_shouldReturnCategoryByIdIfExists() throws Exception {
        mockMvc.perform(get("/category/{category_id}", EXISTING_ENTITY_ID)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("shoes"))
                .andExpect(status().isOk());
    }

    @Test
    @DataSet("database_init.yml")
    void getCategoryById_whenCategoryIdDoesNotExist_shouldReturnNotFoundAndThrowEntityNotFoundException()
            throws Exception {

        MvcResult result = this.mockMvc.perform(get("/category/{category_id}", NONEXISTENT_ENTITY_ID)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        assertThat(result.getResolvedException(), is(instanceOf(EntityNotFoundException.class)));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ROLE_ADMIN})
    @Transactional
    @DataSet("database_init.yml")
    @ExpectedDataSet(value = "category/create_category.yml")
    void createCategory_shouldCreateValidCategory() throws Exception {
        CategoryDto nonExistCategoryDto = createNonExistValidCategoryDto();
        mockMvc.perform(post("/category")
                .content(asJsonString(nonExistCategoryDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DataSet("database_init.yml")
    void createCategory_whenUserDoesNotHaveRoleAdmin_shouldReturnUnauthorized() throws Exception {
        CategoryDto nonExistCategoryDto = createNonExistValidCategoryDto();
        mockMvc.perform(post("/category")
                .content(asJsonString(nonExistCategoryDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ROLE_ADMIN})
    @Transactional
    @DataSet("database_init.yml")
    void createCategory_whenCategoryIdNotEqualsZero_shouldReturnBadRequestAndThrowInvalidDtoException()
            throws Exception {

        CategoryDto nonExistCategoryDtoWithInvalidId = createNonExistCategoryDtoWithInvalidId();

        MvcResult result = mockMvc.perform(post("/category")
                .content(asJsonString(nonExistCategoryDtoWithInvalidId))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(result.getResolvedException(), is(instanceOf(InvalidDtoException.class)));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ROLE_ADMIN})
    @Transactional
    @DataSet("database_init.yml")
    void createCategory_whenInternalSubcategoryIdNotEqualsZero_shouldReturnBadRequest()
            throws Exception {

        CategoryDto nonExistCategoryDtoWithInvalidId = createNonExistCategoryDtoWithInvalidSubcategoryId();

        mockMvc.perform(post("/category")
                .content(asJsonString(nonExistCategoryDtoWithInvalidId))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ROLE_ADMIN})
    @Transactional
    @DataSet("database_init.yml")
    void createCategory_whenCategoryNameHasDuplicate_shouldReturnBadRequest()
            throws Exception {

        CategoryDto nonExistCategoryDtoWithDuplicateName = createCategoryDtoWithDuplicateName();

        mockMvc.perform(post("/category")
                .content(asJsonString(nonExistCategoryDtoWithDuplicateName))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ROLE_ADMIN})
    @Transactional
    @DataSet("database_init.yml")
    @ExpectedDataSet(value = "category/update_category.yml")
    void updateCategory_shouldUpdateExistedCategory() throws Exception {

        CategoryDto updatedCategoryDto = getUpdatedCategoryDto(EXISTING_ENTITY_ID,
                EXISTING_ENTITY_ID,
                "footwear");

        mockMvc.perform(put("/category")
                .content(asJsonString(updatedCategoryDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.name").value("footwear"))
                .andExpect(jsonPath("$.subcategories", hasSize(2)));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ROLE_ADMIN})
    @Transactional
    @DataSet("database_init.yml")
    void updateCategory_whenCategoryIdDoesNotExist_shouldReturnBadRequestAndThrowIllegalIdentifierException()
            throws Exception {

        CategoryDto updatedCategoryDto = getUpdatedCategoryDto(EXISTING_ENTITY_ID,
                NONEXISTENT_ENTITY_ID,
                NEW_CATEGORY_NAME);

        MvcResult result = mockMvc.perform(put("/category")
                .content(asJsonString(updatedCategoryDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(result.getResolvedException(), is(instanceOf(IllegalIdentifierException.class)));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ROLE_ADMIN})
    @Transactional
    @DataSet("database_init.yml")
    void updateCategory_whenSubcategoryIdDoesNotExistAndNotEqualsZero_shouldReturnBadRequest()
            throws Exception {

        CategoryDto updatedCategoryDto = getUpdatedCategoryDto(NONEXISTENT_ENTITY_ID,
                EXISTING_ENTITY_ID,
                NEW_CATEGORY_NAME);

        mockMvc.perform(put("/category")
                .content(asJsonString(updatedCategoryDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ROLE_ADMIN})
    @Transactional
    @DataSet("database_init.yml")
    void deleteCategoryById_shouldDeleteExistedCategory() throws Exception {
        mockMvc.perform(delete("/category/{category_id}", 2L)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ROLE_ADMIN})
    @Transactional
    @DataSet("database_init.yml")
    void deleteCategory_whenCategoryIdDoesNotExist_shouldReturnBadRequestAndThrowInvalidDtoException()
            throws Exception {

        MvcResult result = this.mockMvc.perform(delete("/category/{category_id}", NONEXISTENT_ENTITY_ID)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(result.getResolvedException(), is(instanceOf(InvalidDtoException.class)));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ROLE_ADMIN})
    @Transactional
    @DataSet("database_init.yml")
    void deleteCategory_whenInternalSubcategoryHasProducts_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(delete("/category/{category_id}", EXISTING_ENTITY_ID)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
