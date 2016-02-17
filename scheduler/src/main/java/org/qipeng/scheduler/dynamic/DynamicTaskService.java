package org.qipeng.scheduler.dynamic;


public interface DynamicTaskService {
	void addTask(TaskDefinition taskDefinition);  //添加一个任务
    void updateTask(TaskDefinition taskDefinition); //修改任务
    void delete(Long ...ids);  //删除任务
    Iterable<TaskDefinition> findAll(); //获取所有的任务定义
    Long getAllTaskCount(); //获得任务数量
    Long getStartTaskCount();//获得状态是可执行状态的任务的数量
    void startTask(Long ...ids);//将任务状态变为可执行状态
    void stopTask(Long ...ids); //将任务状态变为停止状态
    void initTask();//初始话任务数据
    TaskDefinition get(Long id);//根据id获得任务
    void immediatelyExecute(Long id);//忽略任务状态，立即执行一次
    void deleteAll();
}
