package com.github.solenra.server.util;

import org.springframework.http.HttpStatus;

import com.github.solenra.server.exceptions.ApplicationException;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class TaskRejectedExecutionHandler implements RejectedExecutionHandler {

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Task rejected", "Task " + r.toString() + " rejected from " + executor.toString());
	}

}
