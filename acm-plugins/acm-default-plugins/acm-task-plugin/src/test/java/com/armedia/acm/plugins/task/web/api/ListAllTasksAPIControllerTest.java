package com.armedia.acm.plugins.task.web.api;

/*-
 * #%L
 * ACM Default Plugin: Tasks
 * %%
 * Copyright (C) 2014 - 2018 ArkCase LLC
 * %%
 * This file is part of the ArkCase software. 
 * 
 * If the software was purchased under a paid ArkCase license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.armedia.acm.plugins.task.model.AcmApplicationTaskEvent;
import com.armedia.acm.plugins.task.model.AcmTask;
import com.armedia.acm.plugins.task.model.NumberOfDays;
import com.armedia.acm.plugins.task.service.TaskDao;
import com.armedia.acm.plugins.task.service.TaskEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by marjan.stefanoski on 8/20/2014.
 */
public class ListAllTasksAPIControllerTest extends EasyMockSupport
{

    private MockMvc mockMvc;
    private MockHttpSession mockHttpSession;

    private ListAllTasksAPIController unit;

    private TaskDao mockTaskDao;
    private TaskEventPublisher mockTaskEventPublisher;
    private Authentication mockAuthentication;

    @Autowired
    private ExceptionHandlerExceptionResolver exceptionResolver;

    private Logger log = LogManager.getLogger(getClass());

    @Before
    public void setUp() throws Exception
    {
        mockTaskDao = createMock(TaskDao.class);
        mockTaskEventPublisher = createMock(TaskEventPublisher.class);
        mockHttpSession = new MockHttpSession();
        mockAuthentication = createMock(Authentication.class);

        unit = new ListAllTasksAPIController();

        unit.setTaskDao(mockTaskDao);
        unit.setTaskEventPublisher(mockTaskEventPublisher);

        mockMvc = MockMvcBuilders.standaloneSetup(unit).setHandlerExceptionResolvers(exceptionResolver).build();
    }

    @Test
    public void dueInAMonthTasksTest() throws Exception
    {
        String user = "user";

        AcmTask userTask = new AcmTask();
        userTask.setAssignee(user);
        userTask.setTaskId(500L);
        userTask.setDueDate(new Date());
        String ipAddress = "ipAddress";

        expect(mockTaskDao.dueSpecificDateTasks(NumberOfDays.THIRTY_DAYS)).andReturn(Arrays.asList(userTask));
        mockTaskEventPublisher.publishTaskEvent(anyObject(AcmApplicationTaskEvent.class));

        mockHttpSession.setAttribute("acm_ip_address", ipAddress);

        // MVC test classes must call getName() somehow
        expect(mockAuthentication.getName()).andReturn("user").atLeastOnce();

        replayAll();

        MvcResult result = mockMvc.perform(
                get("/api/v1/plugin/task/list/dueInAMonth")
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .principal(mockAuthentication)
                        .session(mockHttpSession))
                .andReturn();

        verifyAll();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE));

        String returned = result.getResponse().getContentAsString();

        log.info("results: " + returned);

        ObjectMapper objectMapper = new ObjectMapper();

        List<AcmTask> foundTasks = objectMapper.readValue(returned,
                objectMapper.getTypeFactory().constructParametricType(List.class, AcmTask.class));

        assertEquals(1, foundTasks.size());

        AcmTask found = foundTasks.get(0);
        assertEquals(userTask.getTaskId(), found.getTaskId());
    }

    @Test
    public void dueInAWeekTasksTest() throws Exception
    {
        String user = "user";

        AcmTask userTask = new AcmTask();
        userTask.setAssignee(user);
        userTask.setTaskId(500L);
        userTask.setDueDate(new Date());
        String ipAddress = "ipAddress";

        expect(mockTaskDao.dueSpecificDateTasks(NumberOfDays.SEVEN_DAYS)).andReturn(Arrays.asList(userTask));
        mockTaskEventPublisher.publishTaskEvent(anyObject(AcmApplicationTaskEvent.class));

        mockHttpSession.setAttribute("acm_ip_address", ipAddress);

        // MVC test classes must call getName() somehow
        expect(mockAuthentication.getName()).andReturn("user").atLeastOnce();

        replayAll();

        MvcResult result = mockMvc.perform(
                get("/api/v1/plugin/task/list/dueInAWeek")
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .principal(mockAuthentication)
                        .session(mockHttpSession))
                .andReturn();

        verifyAll();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE));

        String returned = result.getResponse().getContentAsString();

        log.info("results: " + returned);

        ObjectMapper objectMapper = new ObjectMapper();

        List<AcmTask> foundTasks = objectMapper.readValue(returned,
                objectMapper.getTypeFactory().constructParametricType(List.class, AcmTask.class));

        assertEquals(1, foundTasks.size());

        AcmTask found = foundTasks.get(0);
        assertEquals(userTask.getTaskId(), found.getTaskId());
    }

    @Test
    public void dueTomorrowTasksTest() throws Exception
    {
        String user = "user";

        AcmTask userTask = new AcmTask();
        userTask.setAssignee(user);
        userTask.setTaskId(500L);
        userTask.setDueDate(new Date());
        String ipAddress = "ipAddress";

        expect(mockTaskDao.dueSpecificDateTasks(NumberOfDays.ONE_DAY)).andReturn(Arrays.asList(userTask));
        mockTaskEventPublisher.publishTaskEvent(anyObject(AcmApplicationTaskEvent.class));

        mockHttpSession.setAttribute("acm_ip_address", ipAddress);

        // MVC test classes must call getName() somehow
        expect(mockAuthentication.getName()).andReturn("user").atLeastOnce();

        replayAll();

        MvcResult result = mockMvc.perform(
                get("/api/v1/plugin/task/list/dueTomorrow")
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .principal(mockAuthentication)
                        .session(mockHttpSession))
                .andReturn();

        verifyAll();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE));

        String returned = result.getResponse().getContentAsString();

        log.info("results: " + returned);

        ObjectMapper objectMapper = new ObjectMapper();

        List<AcmTask> foundTasks = objectMapper.readValue(returned,
                objectMapper.getTypeFactory().constructParametricType(List.class, AcmTask.class));

        assertEquals(1, foundTasks.size());

        AcmTask found = foundTasks.get(0);
        assertEquals(userTask.getTaskId(), found.getTaskId());
    }

    @Test
    public void pastDueTasksTest() throws Exception
    {
        String user = "user";

        AcmTask userTask = new AcmTask();
        userTask.setAssignee(user);
        userTask.setTaskId(500L);
        userTask.setDueDate(new Date());
        String ipAddress = "ipAddress";

        expect(mockTaskDao.pastDueTasks()).andReturn(Arrays.asList(userTask));
        mockTaskEventPublisher.publishTaskEvent(anyObject(AcmApplicationTaskEvent.class));

        mockHttpSession.setAttribute("acm_ip_address", ipAddress);

        // MVC test classes must call getName() somehow
        expect(mockAuthentication.getName()).andReturn("user").atLeastOnce();

        replayAll();

        MvcResult result = mockMvc.perform(
                get("/api/v1/plugin/task/list/pastDue")
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .principal(mockAuthentication)
                        .session(mockHttpSession))
                .andReturn();

        verifyAll();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE));

        String returned = result.getResponse().getContentAsString();

        log.info("results: " + returned);

        ObjectMapper objectMapper = new ObjectMapper();

        List<AcmTask> foundTasks = objectMapper.readValue(returned,
                objectMapper.getTypeFactory().constructParametricType(List.class, AcmTask.class));

        assertEquals(1, foundTasks.size());

        AcmTask found = foundTasks.get(0);
        assertEquals(userTask.getTaskId(), found.getTaskId());
    }

    @Test
    public void allTasks() throws Exception
    {
        String user = "user";

        AcmTask userTask = new AcmTask();
        userTask.setAssignee(user);
        userTask.setTaskId(500L);
        userTask.setDueDate(new Date());
        String ipAddress = "ipAddress";

        expect(mockTaskDao.allTasks()).andReturn(Arrays.asList(userTask));
        mockTaskEventPublisher.publishTaskEvent(anyObject(AcmApplicationTaskEvent.class));

        mockHttpSession.setAttribute("acm_ip_address", ipAddress);

        // MVC test classes must call getName() somehow
        expect(mockAuthentication.getName()).andReturn("user").atLeastOnce();

        replayAll();

        MvcResult result = mockMvc.perform(
                get("/api/v1/plugin/task/list/all")
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .principal(mockAuthentication)
                        .session(mockHttpSession))
                .andReturn();

        verifyAll();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE));

        String returned = result.getResponse().getContentAsString();

        log.info("results: " + returned);

        ObjectMapper objectMapper = new ObjectMapper();

        List<AcmTask> foundTasks = objectMapper.readValue(returned,
                objectMapper.getTypeFactory().constructParametricType(List.class, AcmTask.class));

        assertEquals(1, foundTasks.size());

        AcmTask found = foundTasks.get(0);
        assertEquals(userTask.getTaskId(), found.getTaskId());
    }
}
