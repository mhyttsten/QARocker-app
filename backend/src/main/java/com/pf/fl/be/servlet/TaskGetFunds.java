package com.pf.fl.be.servlet;

import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;

/**
 * Form Handling Servlet
 * This servlet has one method
 * {@link #doPost(<#HttpServletRequest req#>, <#HttpServletResponse resp#>)} which takes the form
 * submisson from /src/main/webapp/tasks.jsp to add and delete tasks.
 */
public class TaskGetFunds extends HttpServlet {

    private static final Logger log = Logger.getLogger(TaskGetFunds.class.getName());
    private static final int numberOfTasksToAdd = 100;
    private static final int numberOfTasksToLease = 100;
    private static boolean useTaggedTasks = true;
    private static String output;
    private static String message;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
            ServletException {

        log.info("TaskGetFunds.doPost entered");

        // Pull tasks from the Task Queue and process them
        Queue q = QueueFactory.getQueue("getfunds");
        List<TaskHandle> tasks = q.leaseTasks(585, TimeUnit.SECONDS, Long.MAX_VALUE); // 9m 45s
        log.info("We have leased: " + tasks.size() + " tasks");

        boolean isFirst = true;
        for (TaskHandle task : tasks) {
            if (isFirst) {
                isFirst = false;
                log.info("Payload was: " + task.getPayload());
                log.info("Going to sleep: " + new Date().toString());
                try { Thread.sleep(9*60*1000); } catch(Exception exc) { }
                log.info("Woke up: " + new Date().toString());
            }

            log.info("Deleting task");
            q.deleteTask(task);
        }
    }
}
