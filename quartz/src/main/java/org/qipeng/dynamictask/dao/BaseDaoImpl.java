package org.qipeng.dynamictask.dao;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;

@SuppressWarnings("unchecked")
@Repository
public class BaseDaoImpl implements IBaseDao {
	
	@Resource(name = "sessionFactory")
	private SessionFactory sessionFactory;
	
	private Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	public Serializable save(Object entity) {
		return getSession().save(entity);
	}

	@Override
	public List<Serializable> saveList(Collection<?> collection) {
		List<Serializable> pkList = new ArrayList<Serializable>();
		int count = 0;
		for (Object obj : collection) {
			Serializable pk = getSession().save(obj);
			pkList.add(pk);
			if(++count%10==0){
				getSession().flush();
			}
		}
		return pkList;
	}

	@Override
	public void saveOrUpdate(Object entity) {
		getSession().saveOrUpdate(entity);
	}

	@Override
	public void delete(Object entity) {
		getSession().delete(entity);
	}

	@Override
	public void update(Object entity) {
		getSession().update(entity);
	}

	@Override
	public int updateBySql(String sql, Object... params) {
		Query query = getSession().createSQLQuery(sql);
		setParameters(query,params);
		return query.executeUpdate();
	}

	@Override
	public int updateBySql(String sql, Map<String, Object> alias) {
		Query query = getSession().createSQLQuery(sql);
		setAlias(query,alias);
		return query.executeUpdate();
	}

	@Override
	public int updateByHql(String hql, Object... params) {
		Query query = getSession().createQuery(hql);
		setParameters(query,params);
		return query.executeUpdate();
	}

	@Override
	public int updateByHql(String hql, Map<String, Object> alias) {
		Query query = getSession().createQuery(hql);
		setAlias(query,alias);
		return query.executeUpdate();
	}
	
	/**
	 * 由于Hibernate会将所有查询出来的字段的别名转换成大写，导致queryBySqlReturnMap和queryBySqlReturnVo
	 * 返回的对象无法被注入，所以此处进行处理，为sql的别名添加双引号
	 * @param sql
	 * @return
	 */
	private String addQuotationMarks(String sql){
		
		StringBuffer buffer = new StringBuffer();
		
		int fromIndex = StringUtils.indexOfIgnoreCase(sql,"from");
	
		String selectSql = StringUtils.trim(StringUtils.substring(sql,0,fromIndex));
		String fromSql = StringUtils.substring(sql,fromIndex);

		String[] tmp = StringUtils.split(selectSql,",");
		
		for(int i=0;i<tmp.length;i++){
			String string = tmp[i];
			int lastSpaceIndex = StringUtils.lastIndexOf(string," ");
			String a = StringUtils.substring(string,0,lastSpaceIndex+1);
			buffer.append(a);
			String b = StringUtils.substring(string,lastSpaceIndex+1);
			buffer.append("\""+b+"\"");
			if(i!=tmp.length-1){
				buffer.append(",");
			}
		}
		buffer.append(" "+fromSql);
		return buffer.toString();
	}
	
	
	/**
	 * 将Object对象转换成Collection（注意Object实际上是一个数组）
	 * @param obj
	 * @return
	 */
	private Collection<?> converToCollection(Object obj){
		 List<Object> newList = new ArrayList<Object>();
		 for(int i=0;i<Array.getLength(obj);i++){
			 newList.add(Array.get(obj,i));
		 }
		 return newList;
	}
	
	
	/**
	 * 设置？号占位符的参数
	 * @param query
	 * @param args
	 */
	private void setParameters(Query query, Object... args) {
		if(args!=null && args.length>0){
			for(int i=0;i<args.length;i++){
				query.setParameter(i,args[i]);
			}
		}
	}

	/**
	 * 设置别名参数
	 * @param query
	 * @param alias
	 */
	private void setAlias(Query query, Map<String, Object> alias) {
		if(alias!=null){
			for(Entry<String,Object> entry:alias.entrySet()){
				String key = entry.getKey();
				Object value = entry.getValue();
				if(value.getClass().isArray()){
					query.setParameterList(key,converToCollection(value));
					//query.setParameterList(key,(Object[])value);
				}else if(value instanceof Collection){
					query.setParameterList(key,(Collection<?>)value);
				}else{
					query.setParameter(key,value);
				}
			}
		}
	}
	
	@Override
	public Integer executeProc(String procedure, List<Object> params, List<Integer> outs) {
		
		try {
//			Connection con=getSession().connection();
			Connection con= SessionFactoryUtils.getDataSource(getSession().getSessionFactory()).getConnection();
			CallableStatement cstmt = con.prepareCall(procedure);

			//设置输入参数
			if(params!=null)
				{
				for (int i = 0; i < params.size(); i++) {
					Object param = params.get(i);
					if(param.getClass().isInstance(String.class))
					{
						cstmt.setString(i + 1, (String)param);
					} else if(param.getClass().isInstance(Integer.class))
					{
						cstmt.setInt(i + 1, (Integer)param);
					}
					
				}
			}
			
			//设置输出参数
			if(outs!=null)
			{
				for (int i = 0; i < outs.size(); i++) {
					Integer out = outs.get(i);
					cstmt.registerOutParameter(i + 1, out);
				}
			}
			
	        return cstmt.executeUpdate();
			
		} catch (HibernateException | SQLException e) {
			e.printStackTrace();
		}
		return null;
	
	}
	
	@Override
	public CallableStatement executePackage(String procedure, List<Object> params, List<Integer> outs) {
		
		try {
			Connection con= DataSourceUtils.getConnection(SessionFactoryUtils.getDataSource(getSession().getSessionFactory()));
			try {
				CallableStatement cstmt = con.prepareCall(procedure);
				int count = 1;
				//设置输出参数
				if(outs!=null)
				{
					for (int i = 0; i < outs.size(); i++) {
						Integer out = outs.get(i);
						cstmt.registerOutParameter(count, out);
						count += 1;
					}
				}
				
				//设置输入参数
				if(params!=null)
					{
					for (int i = 0; i < params.size(); i++) {
						Object param = params.get(i);
						if(param instanceof String)
						{
							cstmt.setString(count, (String)param);
						} else if(param instanceof Integer)
						{
							cstmt.setInt(count, (Integer)param);
						}
						
						count += 1;
						
					}
				}
				
				cstmt.executeUpdate();
				
		        return cstmt;
			} finally {
				DataSourceUtils.doReleaseConnection(con, SessionFactoryUtils.getDataSource(getSession().getSessionFactory()));
			}
		} catch (HibernateException | SQLException e) {
			e.printStackTrace();
		}
		return null;
	
	}

	@Override
	public <T> List<T> executeQuery(String nameQuery, List<Object> params) {

		Query query= null;
        //使用存储过程获取数据
        query=getSession().getNamedQuery(nameQuery);

        //设置输入参数
		if(params!=null)
			{
			for (int i = 0; i < params.size(); i++) {
				Object param = params.get(i);
				query.setString(i + 1, (String)param);
			}
		}
		
		return query.list();
	}

	/**
	 * 执行有传入，传出参数的存储过程（参数类型只支持整型与字符型）
	 * @param procedure 执行语句
	 * @param jdbcTypes 参数数据类型
	 * @param inOutFlags 传入传出标志
	 * @param params 参数值
	 * @see 参数类型、标志、及参数值list需保持一致
	 */
	@Override
	public CallableStatement executeProcedure(String procedure,
			List<Integer> jdbcTypes, List<String> inOutFlags,
			List<Object> params) {
		try {
			Connection con= DataSourceUtils.getConnection(SessionFactoryUtils.getDataSource(getSession().getSessionFactory()));
			try {
				CallableStatement cstmt = con.prepareCall(procedure);
				int count = 1;
				//设置输入输出参数
				for (int i = 0; i < jdbcTypes.size(); i++) {
					if ("IN".equals(inOutFlags.get(i))) {
						if(Types.INTEGER == jdbcTypes.get(i).intValue()) {
							cstmt.setInt(count, (Integer)params.get(i));
						}
						else if(Types.VARCHAR == jdbcTypes.get(i).intValue()) {
							cstmt.setString(count, (String)params.get(i));
						}
					} else {
						cstmt.registerOutParameter(count, jdbcTypes.get(i).intValue());
					}
					count += 1;
				}
				
				cstmt.executeUpdate();
				
		        return cstmt;
			} finally {
				DataSourceUtils.doReleaseConnection(con, SessionFactoryUtils.getDataSource(getSession().getSessionFactory()));
			}
		} catch (HibernateException | SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public <T> T get(Class<T> entityClass, Serializable id) {
		return (T) getSession().get(entityClass, id);
	}

	@Override
	public Long getCount(String hql, Object... param) {
		Query query = getSession().createQuery("select count(*) "+hql);
		setParameters(query,param);
		return (Long) query.uniqueResult();
	}

	@Override
	public <T> T load(Class<T> entityClass, Serializable id) {
		return (T) getSession().load(entityClass, id);
	}

	@Override
	public <T> List<T> findAll(Class<T> entityClass) {
		return getSession().createQuery("from "+entityClass.getSimpleName()).list();
	}


	@Override
	public <T> T getByHql(String hql, Object... param) {
		Query query = getSession().createQuery(hql);
		setParameters(query, param);
		return (T) query.uniqueResult();
	}

	@Override
	public <T> T getByProperty(Class<T> entityClass, Map<String, Object> paramMap) {
		StringBuffer hql = new StringBuffer();
		hql.append("from " + entityClass.getSimpleName() + " where 1=1 ");
		for(Entry<String,Object> entry : paramMap.entrySet()){
			hql.append(" and "+entry.getKey()+"= :"+entry.getKey());
		}
		Query query = getSession().createQuery(hql.toString());
		setAlias(query, paramMap);
		return (T) query.uniqueResult();
	}

	@Override
	public <T> T getByProperty(Class<T> entityClass, String propertyName, Object propertyValue) {
		String hql = "from " + entityClass.getSimpleName() + " where "+propertyName+"=?";
		return (T) getSession().createQuery(hql).setParameter(0,propertyValue).uniqueResult();
	}

	@Override
	public <T> T getByHql(String hql, Map<String, Object> alias) {
		Query query = getSession().createQuery(hql);
		setAlias(query, alias);
		return (T) query.uniqueResult();
	}

	@Override
	public void deleteList(Collection<?> collection) {
		int count = 0;
		for (Object object : collection) {
			getSession().delete(object);
			if(++count%10==0){
				getSession().flush();
			}
		}
	}

	@Override
	public Long getCount(String hql, Map<String, Object> alias) {
		hql = "select count(*) "+hql;
		Query query = getSession().createQuery(hql);
		setAlias(query, alias);
		return (Long) query.uniqueResult();
	}

	@Override
	public <T> List<T> findListByHql(String hql, Object... params) {
		Query query = getSession().createQuery(hql);
		setParameters(query,params);
		return query.list();
	}

	@Override
	public <T> List<T> findListByHql(String hql, Map<String, Object> alias) {
		Query query = getSession().createQuery(hql);
		setAlias(query, alias);
		return query.list();
	}

	@Override
	public <T> List<T> findListBySql(String sql, Object... params) {
		Query query = getSession().createSQLQuery(sql);
		setParameters(query,params);
		return query.list();
	}

	@Override
	public <T> List<T> findListBySql(String sql, Map<String, Object> alias) {
		Query query = getSession().createSQLQuery(sql);
		setAlias(query, alias);
		return query.list();
	}

	@Override
	public <T> List<T> findListByProperty(Class<T> entityClass, String propertyName, Object propertyValue) {
		String hql = "from " + entityClass.getSimpleName() + " where "+propertyName+"=?";
		Query query = getSession().createQuery(hql);
		setParameters(query,propertyValue);
		return query.list();
	}

	@Override
	public <T> List<T> findListByProperty(Class<T> entityClass, Map<String, Object> propertyMap) {
		StringBuffer hql = new StringBuffer("from "+entityClass.getSimpleName()+" where 1=1 ");
		for(String key:propertyMap.keySet()){
			hql.append(" and "+key+"=:"+key);
		}
		Query query = getSession().createQuery(hql.toString());
		setAlias(query, propertyMap);
		return query.list();
	}

	@Override
	public List<Object[]> queryBySql(String sql, Object... params) {
		Query query = getSession().createSQLQuery(sql);
		setParameters(query,params);
		return query.list();
	}

	@Override
	public List<Object[]> queryBySql(String sql, Map<String, Object> alias) {
		Query query = getSession().createSQLQuery(sql);
		setAlias(query, alias);
		return query.list();
	}

	@Override
	public List<Map<String, Object>> queryBySqlReturnMap(String sql, Object... params) {
		Query query = getSession().createSQLQuery(addQuotationMarks(sql));
		setParameters(query,params);
		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		return query.list();
	}

	@Override
	public List<Map<String, Object>> queryBySqlReturnMap(String sql, Map<String, Object> alias) {
		Query query = getSession().createSQLQuery(addQuotationMarks(sql));
		setAlias(query, alias);
		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		return query.list();
	}

	@Override
	public <N> List<N> queryBySqlReturnVo(String sql, Class<N> clz, Object... params) {
		Query query = getSession().createSQLQuery(addQuotationMarks(sql));
		setParameters(query,params);
		query.setResultTransformer(Transformers.aliasToBean(clz));
		return query.list();
	}

	@Override
	public <N> List<N> queryBySqlReturnVo(String sql, Class<N> clz, Map<String, Object> alias) {
		Query query = getSession().createSQLQuery(addQuotationMarks(sql));
		setAlias(query, alias);
		query.setResultTransformer(Transformers.aliasToBean(clz));
		return query.list();
	}

}
