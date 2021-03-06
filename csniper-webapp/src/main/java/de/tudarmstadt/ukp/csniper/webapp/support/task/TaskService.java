/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.csniper.webapp.support.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.csniper.webapp.support.task.Task.Message;


/**
 * <p>
 * Should be registered as SESSION scoped bean to prevent memory leaks with unfinished tasks.
 * </p>
 * 
 * TODO automatic cleanup of tasks
 * 
 * @author Christopher Hlubek (hlubek)
 * 
 */
/*
 * Changes:
 * - REC: avoid NPE in getMessages() if no tasks with specified ID exists.
 * - REC: made getTask() public
 */
public class TaskService implements ITaskService
{

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);

	private final Executor executor;

	public TaskService(Executor executor)
	{
		this.executor = executor;
	}

	private Map<Long, Task> tasks = new HashMap<Long, Task>();

	private long nextTaskId = 0;

	@Override
	public synchronized Long schedule(Task task)
	{
		Long taskId = nextTaskId();
		LOGGER.debug("Scheduling task with ID: " + taskId);
		tasks.put(taskId, task);
		return taskId;
	}

	@Override
	public void start(Long taskId)
	{
		Task task = getTask(taskId);
		if (task != null)
		{
			LOGGER.debug("Starting task with ID: " + taskId);
			start(task);
		}
		else
		{
			LOGGER.warn("Task ID " + taskId + " not found");
		}
	}

	@Override
	public void cancel(Long taskId)
	{
		Task task = getTask(taskId);
		if (task != null)
		{
			task.cancel();
		}
	}

	private void start(Task task)
	{
		executor.execute(task.getRunnable());
	}

	public Task getTask(Long taskId)
	{
		return tasks.get(taskId);
	}


	private synchronized long nextTaskId()
	{
		return nextTaskId++;
	}

	@Override
	public Long scheduleAndStart(Task task)
	{
		Long taskId = schedule(task);
		start(task);
		return taskId;
	}

	@Override
	public void finish(Long taskId)
	{
		tasks.remove(taskId);
	}

	@Override
	public Progression getProgression(Long taskId)
	{
		Task task = getTask(taskId);
		// HACK Need real finished setting in Progression
		// FIXME we really don't know if task is null!
		return (task == null) ? new Progression(0, 0, true) : new Progression(
			task.getCurrent(), task.getTotal(), task.isDone());
	}

	@Override
	public List<Message> getMessages(Long taskId)
	{
		Task task = getTask(taskId);
		if (task != null) {
			// FIXME clone messages to prevent direct reference to task!
			return task.getMessages();
		}
		else {
			return new ArrayList<Message>(0);
		}
	}
}
