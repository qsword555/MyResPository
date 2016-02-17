package org.qipeng.scheduler.dynamic;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TaskDefinitionDao extends CrudRepository<TaskDefinition, Long>{

	@Modifying
	@Query("update TaskDefinition set status=?1 where id in (?2)")
	public int updateStatus(boolean newStatus,Long[] ids);
	
	@Query("select count(id) from TaskDefinition where status = true")
	public Long getCountStatusOk();
}
