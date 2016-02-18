package org.qipeng.dynamictask.dao;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 基于Hibernate操作数据库的共通方法
 * 本共通方法中所有get或者load开头的方法均返回1个对象
 * find开头的方法返回一组对象
 * @author Administrator
 *
 */
public interface IBaseDao {
	
	/***************** 写库操作 开始 *******************/
	
	/**
	 * 保存一个实体
	 * @param entity
	 * @return 保存成功后的实体的主键
	 */
	Serializable save(Object obj);
	
	/**
	 * 批量保存实体
	 * @param list
	 * @return 保存成功后的实体主键集合
	 */
	List<Serializable> saveList(Collection<?> collection);
	
	/**
	 * 主键不存在的时候执行保存操作
	 * 主键存在的时候执行更新操作
	 * @param entity
	 */
	void saveOrUpdate(Object obj);
	
	/**
	 * 删除一个实体
	 * @param obj
	 */
	void delete(Object obj);
	
	/**
	 * 批量删除实体
	 * @param collection
	 */
	void deleteList(Collection<?> collection);
	
	/**
	 * 更新一个实体
	 * @param entity
	 */
	void update(Object entity);
	
	/**
	 * 基于sql的更新使用？号作为占位符
	 * @param sql
	 * @param params
	 * @return
	 */
	int updateBySql(String sql, Object... params);
	
	/**
	 * 基于sql的更新，使用别名作为占位符，使用在参数列表中有一个是数组的情况
	 * 例如：
	 * @param sql
	 * @param alias 注意map中的value如果是数组，只能是对象数组，例如Integer[]，绝不能是int[]
	 * @return
	 */
	int updateBySql(String sql,Map<String,Object> alias);
	
	/**使用问号作为占位符 */
	int updateByHql(String hql, Object... params);
	
	/**使用别名作为占位符 */
	int updateByHql(String hql,Map<String,Object> alias);
	
	
	/***************** 读库操作 开始 *******************/
	
	/**
	 * load开头的方法都返回单一实体
	 * 延迟加载一个实体，实体不存在报错
	 * @param entityClass
	 * @param id
	 * @return
	 */
	<T> T load(Class<T> entityClass, Serializable id);
	
	/**
	 * get开头的方法都返回单一实体
	 * 根绝ID获得一个实体（如果实体不存在返回null）
	 * @param entityClass
	 * @param id
	 * @return
	 */
	<T> T get(Class<T> entityClass, Serializable id);
	
	/**
	 * get开头的方法都返回单一实体
	 * hql基于？号占位符
	 * @param hql
	 * @param param
	 * @return
	 */
	<T> T getByHql(String hql,Object ...param);
	
	/**
	 * get开头的方法都返回单一实体
	 * hql基于？号占位符
	 * @param hql
	 * @param param
	 * @return
	 */
	<T> T getByHql(String hql,Map<String,Object> alias);
	
	/**
	 * get开头的方法都返回单一实体
	 * 根据实体类和实体类的属性名或者数据库表字段名称来进行查询，(单条件)
	 * @param entityClass
	 * @param propertyName  属性名或者数据库表字段名
	 * @param propertyValue 值
	 * @return
	 */
	<T> T getByProperty(Class<T> entityClass,String propertyName,Object propertyValue);
	
	/**
	 * get开头的方法都返回单一实体
	 * 根据实体类和实体类的属性名或者数据库表字段名称来进行查询，(多条件)
	 * @param entityClass
	 * @param paramMap
	 * @return
	 */
	<T> T getByProperty(Class<T> entityClass,Map<String,Object> paramMap);
	
	/**
	 * 查询满足条件的数据的数量（基于？占位符）
	 * @param hql
	 * @param param
	 * @return
	 */
	Long getCount(String hql,Object... param);
	
	/**
	 * 查询满足条件的数据的数量（基于别名占位符）
	 * @param hql
	 * @param param
	 * @return
	 */
	Long getCount(String hql,Map<String,Object> alias);
	
	
	/**
	 * find开头的方法均返回对象集合
	 * 获得所有的实体
	 * @param entityClass
	 * @return
	 */
	<T> List<T> findAll(Class<T> entityClass);
	
	/**
	 * find开头的方法均返回对象集合
	 * 根据属性查询实体集合（单条件）
	 * @param entityClass
	 * @param propertyName 属性名或者数据库表字段名
	 * @param propertyValue
	 * @return
	 */
	<T> List<T> findListByProperty(Class<T> entityClass,String propertyName,Object propertyValue);
	
	/**
	 * find开头的方法均返回对象集合
	 * find开头的方法均返回对象集合
	 * find开头的方法均返回对象集合
	 * 根据属性查询实体集合（多条件）
	 * @param entityClass key可以是属性名或者数据库表字段名
	 * @param propertyMap
	 * @return
	 */
	<T> List<T> findListByProperty(Class<T> entityClass,Map<String,Object> propertyMap);
	
	/**
	 * find开头的方法均返回对象集合
	 * 基于hql的查询实体集合（基于？占位符）
	 * @param hql
	 * @param params
	 * @return
	 */
	<T> List<T> findListByHql(String hql,Object ...params);
	
	/**
	 * find开头的方法均返回对象集合
	 * 基于hql的查询实体集合（基于别名位符）
	 * @param hql
	 * @param params
	 * @return
	 */
	<T> List<T> findListByHql(String hql,Map<String,Object> alias);
	
	/**
	 * find开头的方法均返回对象集合
	 * 基于sql的查询实体集合（基于？占位符）
	 * @param hql
	 * @param params
	 * @return
	 */
	<T> List<T> findListBySql(String sql,Object ...params);
	
	/**
	 * find开头的方法均返回对象集合
	 * 基于sql的查询实体集合（基于别名位符）
	 * @param hql
	 * @param params
	 * @return
	 */
	<T> List<T> findListBySql(String sql,Map<String,Object> alias);
	
	/**
	 * 万能sql查询，返回值为List<Object[]>(基于？号占位符)
	 * @param sql
	 * @param params
	 * @return
	 */
	List<Object[]> queryBySql(String sql,Object ...params);
	
	/**
	 * 万能sql查询，返回值为List<Object[]>(基于别名占位符)
	 * @param sql
	 * @param alias
	 * @return
	 */
	List<Object[]> queryBySql(String sql,Map<String,Object> alias);
	
	/**
	 * 万能sql查询，返回值为List<Map<String,Object>>(基于？号占位符)
	 * 但是要求sql的每个检索出来字段都必须有别名，然后map的key就是此别名
	 * @param sql
	 * @param params
	 * @return
	 */
	List<Map<String,Object>> queryBySqlReturnMap(String sql,Object ...params);
	
	/**
	 * 万能sql查询，返回值为List<Map<String,Object>>(基于别名占位符)
	 * 但是要求sql的每个检索出来字段都必须有别名，然后map的key就是此别名
	 * @param sql
	 * @param alias
	 * @return
	 */
	List<Map<String,Object>> queryBySqlReturnMap(String sql,Map<String,Object> alias);
	
	/**
	 * 注意：由于不同数据库之间的字段类型映射的关系，此方法有可能出现类型转换异常，所以请根据不同的数据库，根据实际情况使用
	 * 可以执行任何复杂的sql，返回值为List<Vo>(基于？占位符)
	 * 但是要求sql的每个检索出来字段都必须有别名，别名的名称就是vo里面属性的名称
	 * @param sql
	 * @param clz   vo的类型 
	 * @param alias
	 * @return
	 */
	<N extends Object>List<N> queryBySqlReturnVo(String sql,Class<N> clz,Object ...params);
	
	/**
	 * 注意：由于不同数据库之间的字段类型映射的关系，此方法有可能出现类型转换异常，所以请根据不同的数据库，根据实际情况使用
	 * 可以执行任何复杂的sql，返回值为List<Vo>(基于别名占位符)
	 * 但是要求sql的每个检索出来字段都必须有别名，别名的名称就是vo里面属性的名称
	 * @param sql
	 * @param clz   vo的类型 
	 * @param alias
	 * @return
	 */
	<N extends Object>List<N> queryBySqlReturnVo(String sql,Class<N> clz,Map<String,Object> alias);
	
	
	/***************** 存储过程操作 开始 *******************/
	
	/**
	 * 执行一个存储过程.
	 * @param procedure
	 * @param params
	 * @param out
	 * @return
	 */
	Integer executeProc(String procedure, List<Object> params, List<Integer> out);
	
	/**
	 * 执行一个Package.
	 * @param procedure
	 * @param params
	 * @param outs
	 * @return
	 */
	CallableStatement executePackage(String procedure, List<Object> params, List<Integer> outs);
	
	/**
	 * 执行一个查询的存储过程。
	 * @param nameQuery
	 * @param params
	 * @return
	 */
	<T> List<T> executeQuery(String nameQuery, List<Object> params);

	/**
	 * 执行有传入，传出参数的存储过程（参数类型只支持整型与字符型）
	 * @param procedure 执行语句
	 * @param jdbcTypes 参数数据类型
	 * @param inOutFlags 传入传出标志
	 * @param params 参数值
	 * @see 参数类型、标志、及参数值list需保持一致
	 */
	CallableStatement executeProcedure(String procedure, List<Integer> jdbcTypes, List<String> inOutFlags, List<Object> params);

}
