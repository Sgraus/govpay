/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2017 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govpay.orm.dao.jdbc;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import java.sql.Connection;

import org.slf4j.Logger;

import org.openspcoop2.utils.sql.ISQLQueryObject;

import org.openspcoop2.generic_project.expression.impl.sql.ISQLFieldConverter;
import org.openspcoop2.generic_project.dao.jdbc.utils.IJDBCFetch;
import org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject;
import org.openspcoop2.generic_project.dao.jdbc.IJDBCServiceSearchWithId;

import it.govpay.orm.IdVersamento;
import it.govpay.orm.IdVistaRiscossione;
import it.govpay.orm.VersamentoIncasso;

import org.openspcoop2.generic_project.utils.UtilsTemplate;
import org.openspcoop2.generic_project.beans.CustomField;
import org.openspcoop2.generic_project.beans.InUse;
import org.openspcoop2.generic_project.beans.IField;
import org.openspcoop2.generic_project.beans.NonNegativeNumber;
import org.openspcoop2.generic_project.beans.UnionExpression;
import org.openspcoop2.generic_project.beans.Union;
import org.openspcoop2.generic_project.beans.FunctionField;
import org.openspcoop2.generic_project.exception.MultipleResultException;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.NotImplementedException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.IExpression;
import org.openspcoop2.generic_project.dao.jdbc.JDBCExpression;
import org.openspcoop2.generic_project.dao.jdbc.JDBCPaginatedExpression;

import org.openspcoop2.generic_project.dao.jdbc.JDBCServiceManagerProperties;
import it.govpay.orm.dao.jdbc.converter.VistaRiscossioniFieldConverter;
import it.govpay.orm.dao.jdbc.fetch.VistaRiscossioniFetch;
import it.govpay.orm.dao.jdbc.JDBCServiceManager;

import it.govpay.orm.VistaRiscossioni;

/**     
 * JDBCVistaRiscossioniServiceSearchImpl
 *
 * @author Giovanni Bussu (bussu@link.it)
 * @author Lorenzo Nardi (nardi@link.it)
 * @author $Author$
 * @version $Rev$, $Date$
 */
public class JDBCVistaRiscossioniServiceSearchImpl implements IJDBCServiceSearchWithId<VistaRiscossioni, IdVistaRiscossione, JDBCServiceManager> {

	private VistaRiscossioniFieldConverter _vistaRiscossioniFieldConverter = null;
	public VistaRiscossioniFieldConverter getVistaRiscossioniFieldConverter() {
		if(this._vistaRiscossioniFieldConverter==null){
			this._vistaRiscossioniFieldConverter = new VistaRiscossioniFieldConverter(this.jdbcServiceManager.getJdbcProperties().getDatabaseType());
		}		
		return this._vistaRiscossioniFieldConverter;
	}
	@Override
	public ISQLFieldConverter getFieldConverter() {
		return this.getVistaRiscossioniFieldConverter();
	}
	
	private VistaRiscossioniFetch vistaRiscossioniFetch = new VistaRiscossioniFetch();
	public VistaRiscossioniFetch getVistaRiscossioniFetch() {
		return this.vistaRiscossioniFetch;
	}
	@Override
	public IJDBCFetch getFetch() {
		return getVistaRiscossioniFetch();
	}
	
	
	private JDBCServiceManager jdbcServiceManager = null;

	@Override
	public void setServiceManager(JDBCServiceManager serviceManager) throws ServiceException{
		this.jdbcServiceManager = serviceManager;
	}
	
	@Override
	public JDBCServiceManager getServiceManager() throws ServiceException{
		return this.jdbcServiceManager;
	}
	

	@Override
	public IdVistaRiscossione convertToId(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, VistaRiscossioni vistaRiscossioni) throws NotImplementedException, ServiceException, Exception{
	
		IdVistaRiscossione idVistaRiscossioni = new IdVistaRiscossione();
		
		idVistaRiscossioni.setCodDominio(vistaRiscossioni.getCodDominio());
		idVistaRiscossioni.setIndiceDati(vistaRiscossioni.getIndiceDati());
		idVistaRiscossioni.setIuv(vistaRiscossioni.getIuv());
	
		return idVistaRiscossioni;
	}
	
	@Override
	public VistaRiscossioni get(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdVistaRiscossione id, org.openspcoop2.generic_project.beans.IDMappingBehaviour idMappingResolutionBehaviour) throws NotFoundException, MultipleResultException, NotImplementedException, ServiceException,Exception {
		Long id_vistaRiscossioni = ( (id!=null && id.getId()!=null && id.getId()>0) ? id.getId() : this.findIdVistaRiscossioni(jdbcProperties, log, connection, sqlQueryObject, id, true));
		return this._get(jdbcProperties, log, connection, sqlQueryObject, id_vistaRiscossioni,idMappingResolutionBehaviour);
		
		
	}
	
	@Override
	public boolean exists(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdVistaRiscossione id) throws MultipleResultException, NotImplementedException, ServiceException,Exception {

		Long id_vistaRiscossioni = this.findIdVistaRiscossioni(jdbcProperties, log, connection, sqlQueryObject, id, false);
		return id_vistaRiscossioni != null && id_vistaRiscossioni > 0;
		
	}
	
	@Override
	public List<IdVistaRiscossione> findAllIds(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCPaginatedExpression expression, org.openspcoop2.generic_project.beans.IDMappingBehaviour idMappingResolutionBehaviour) throws NotImplementedException, ServiceException,Exception {

		List<IdVistaRiscossione> list = new ArrayList<IdVistaRiscossione>();
		
		// default behaviour (id-mapping)
		if(idMappingResolutionBehaviour==null){
			idMappingResolutionBehaviour = org.openspcoop2.generic_project.beans.IDMappingBehaviour.valueOf("USE_TABLE_ID");
		}

		try{
			List<IField> fields = new ArrayList<>();
			fields.add(VistaRiscossioni.model().COD_DOMINIO);
			fields.add(VistaRiscossioni.model().INDICE_DATI);
			fields.add(VistaRiscossioni.model().IUV);

			List<Map<String, Object>> returnMap = this.select(jdbcProperties, log, connection, sqlQueryObject, expression, fields.toArray(new IField[1]));

			for(Map<String, Object> map: returnMap) {
				list.add(this.convertToId(jdbcProperties, log, connection, sqlQueryObject, (VistaRiscossioni)this.getVistaRiscossioniFetch().fetch(jdbcProperties.getDatabase(), VistaRiscossioni.model(), map)));
			}
		} catch(NotFoundException e) {}

        return list;
		
	}
	
	@Override
	public List<VistaRiscossioni> findAll(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCPaginatedExpression expression, org.openspcoop2.generic_project.beans.IDMappingBehaviour idMappingResolutionBehaviour) throws NotImplementedException, ServiceException,Exception {

        List<VistaRiscossioni> list = new ArrayList<VistaRiscossioni>();
        
        // default behaviour (id-mapping)
 		if(idMappingResolutionBehaviour==null){
 			idMappingResolutionBehaviour = org.openspcoop2.generic_project.beans.IDMappingBehaviour.valueOf("USE_TABLE_ID");
 		}
 		try{
 			List<IField> fields = new ArrayList<>();
 			IField idField = new CustomField("id", Long.class, "id", this.getFieldConverter().toTable(VersamentoIncasso.model()));
 	
 			fields.add(idField);
 			fields.add(VistaRiscossioni.model().COD_APPLICAZIONE);
 			fields.add(VistaRiscossioni.model().COD_DOMINIO);
 			fields.add(VistaRiscossioni.model().COD_FLUSSO);
 			fields.add(VistaRiscossioni.model().COD_SINGOLO_VERSAMENTO_ENTE);
 			fields.add(VistaRiscossioni.model().COD_VERSAMENTO_ENTE);
 			fields.add(VistaRiscossioni.model().DATA);
 			fields.add(VistaRiscossioni.model().DATA_REGOLAMENTO);
 			fields.add(VistaRiscossioni.model().FR_IUR);
 			fields.add(VistaRiscossioni.model().IMPORTO_PAGATO);
 			fields.add(VistaRiscossioni.model().IMPORTO_TOTALE_PAGAMENTI);
 			fields.add(VistaRiscossioni.model().INDICE_DATI);
 			fields.add(VistaRiscossioni.model().IUR);
 			fields.add(VistaRiscossioni.model().IUV);
 			fields.add(VistaRiscossioni.model().NUMERO_PAGAMENTI);
 	
 			List<Map<String, Object>> returnMap = this.select(jdbcProperties, log, connection, sqlQueryObject, expression, fields.toArray(new IField[1]));
 	
 			for(Map<String, Object> map: returnMap) {
 				VistaRiscossioni riscossione = (VistaRiscossioni)this.getFetch().fetch(jdbcProperties.getDatabase(), VistaRiscossioni.model(), map);
 				list.add(riscossione);
 			}
 		} catch(NotFoundException e) {}

        return list;      
		
	}
	
	@Override
	public VistaRiscossioni find(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCExpression expression, org.openspcoop2.generic_project.beans.IDMappingBehaviour idMappingResolutionBehaviour) 
		throws NotFoundException, MultipleResultException, NotImplementedException, ServiceException,Exception {

        long id = this.findTableId(jdbcProperties, log, connection, sqlQueryObject, expression);
        if(id>0){
        	return this.get(jdbcProperties, log, connection, sqlQueryObject, id, idMappingResolutionBehaviour);
        }else{
        	throw new NotFoundException("Entry with id["+id+"] not found");
        }
		
	}
	
	@Override
	public NonNegativeNumber count(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCExpression expression) throws NotImplementedException, ServiceException,Exception {
		
		List<Object> listaQuery = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareCount(jdbcProperties, log, connection, sqlQueryObject, expression,
												this.getVistaRiscossioniFieldConverter(), VistaRiscossioni.model());
		
		sqlQueryObject.addSelectCountField(this.getVistaRiscossioniFieldConverter().toTable(VistaRiscossioni.model())+".id","tot",true);
		
		_join(expression,sqlQueryObject);
		
		return org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.count(jdbcProperties, log, connection, sqlQueryObject, expression,
																			this.getVistaRiscossioniFieldConverter(), VistaRiscossioni.model(),listaQuery);
	}

	@Override
	public InUse inUse(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdVistaRiscossione id) throws NotFoundException, NotImplementedException, ServiceException,Exception {
		
		Long id_vistaRiscossioni = this.findIdVistaRiscossioni(jdbcProperties, log, connection, sqlQueryObject, id, true);
        return this._inUse(jdbcProperties, log, connection, sqlQueryObject, id_vistaRiscossioni);
		
	}

	@Override
	public List<Object> select(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCPaginatedExpression paginatedExpression, IField field) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		return this.select(jdbcProperties, log, connection, sqlQueryObject,
								paginatedExpression, false, field);
	}
	
	@Override
	public List<Object> select(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCPaginatedExpression paginatedExpression, boolean distinct, IField field) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		List<Map<String,Object>> map = 
			this.select(jdbcProperties, log, connection, sqlQueryObject, paginatedExpression, distinct, new IField[]{field});
		return org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.selectSingleObject(map);
	}
	
	@Override
	public List<Map<String,Object>> select(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCPaginatedExpression paginatedExpression, IField ... field) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		return this.select(jdbcProperties, log, connection, sqlQueryObject,
								paginatedExpression, false, field);
	}
	
	@Override
	public List<Map<String,Object>> select(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCPaginatedExpression paginatedExpression, boolean distinct, IField ... field) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.setFields(sqlQueryObject,paginatedExpression,field);
		try{
		
			ISQLQueryObject sqlQueryObjectDistinct = 
						org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareSqlQueryObjectForSelectDistinct(distinct,sqlQueryObject, paginatedExpression, log,
												this.getVistaRiscossioniFieldConverter(), field);

			return _select(jdbcProperties, log, connection, sqlQueryObject, paginatedExpression, sqlQueryObjectDistinct);
			
		}finally{
			org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.removeFields(sqlQueryObject,paginatedExpression,field);
		}
	}

	@Override
	public Object aggregate(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCExpression expression, FunctionField functionField) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		Map<String,Object> map = 
			this.aggregate(jdbcProperties, log, connection, sqlQueryObject, expression, new FunctionField[]{functionField});
		return org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.selectAggregateObject(map,functionField);
	}
	
	@Override
	public Map<String,Object> aggregate(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCExpression expression, FunctionField ... functionField) throws ServiceException,NotFoundException,NotImplementedException,Exception {													
		
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.setFields(sqlQueryObject,expression,functionField);
		try{
			List<Map<String,Object>> list = _select(jdbcProperties, log, connection, sqlQueryObject, expression);
			return list.get(0);
		}finally{
			org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.removeFields(sqlQueryObject,expression,functionField);
		}
	}

	@Override
	public List<Map<String,Object>> groupBy(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCExpression expression, FunctionField ... functionField) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		
		if(expression.getGroupByFields().size()<=0){
			throw new ServiceException("GroupBy conditions not found in expression");
		}
		
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.setFields(sqlQueryObject,expression,functionField);
		try{
			return _select(jdbcProperties, log, connection, sqlQueryObject, expression);
		}finally{
			org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.removeFields(sqlQueryObject,expression,functionField);
		}
	}
	

	@Override
	public List<Map<String,Object>> groupBy(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCPaginatedExpression paginatedExpression, FunctionField ... functionField) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		
		if(paginatedExpression.getGroupByFields().size()<=0){
			throw new ServiceException("GroupBy conditions not found in expression");
		}
		
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.setFields(sqlQueryObject,paginatedExpression,functionField);
		try{
			return _select(jdbcProperties, log, connection, sqlQueryObject, paginatedExpression);
		}finally{
			org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.removeFields(sqlQueryObject,paginatedExpression,functionField);
		}
	}
	
	protected List<Map<String,Object>> _select(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
												IExpression expression) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		return _select(jdbcProperties, log, connection, sqlQueryObject, expression, null);
	}
	protected List<Map<String,Object>> _select(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
												IExpression expression, ISQLQueryObject sqlQueryObjectDistinct) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		
		List<Object> listaQuery = new ArrayList<Object>();
		List<JDBCObject> listaParams = new ArrayList<JDBCObject>();
		List<Object> returnField = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareSelect(jdbcProperties, log, connection, sqlQueryObject, 
        						expression, this.getVistaRiscossioniFieldConverter(), VistaRiscossioni.model(), 
        						listaQuery,listaParams);
		
		_join(expression,sqlQueryObject);
        
        List<Map<String,Object>> list = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.select(jdbcProperties, log, connection,
        								org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareSqlQueryObjectForSelectDistinct(sqlQueryObject,sqlQueryObjectDistinct), 
        								expression, this.getVistaRiscossioniFieldConverter(), VistaRiscossioni.model(),
        								listaQuery,listaParams,returnField);
		if(list!=null && list.size()>0){
			return list;
		}
		else{
			throw new NotFoundException("Not Found");
		}
	}
	
	@Override
	public List<Map<String,Object>> union(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
												Union union, UnionExpression ... unionExpression) throws ServiceException,NotFoundException,NotImplementedException,Exception {		
		
		List<ISQLQueryObject> sqlQueryObjectInnerList = new ArrayList<ISQLQueryObject>();
		List<JDBCObject> jdbcObjects = new ArrayList<JDBCObject>();
		List<Class<?>> returnClassTypes = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareUnion(jdbcProperties, log, connection, sqlQueryObject, 
        						this.getVistaRiscossioniFieldConverter(), VistaRiscossioni.model(), 
        						sqlQueryObjectInnerList, jdbcObjects, union, unionExpression);
		
		if(unionExpression!=null){
			for (int i = 0; i < unionExpression.length; i++) {
				UnionExpression ue = unionExpression[i];
				IExpression expression = ue.getExpression();
				_join(expression,sqlQueryObjectInnerList.get(i));
			}
		}
        
        List<Map<String,Object>> list = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.union(jdbcProperties, log, connection, sqlQueryObject, 
        								this.getVistaRiscossioniFieldConverter(), VistaRiscossioni.model(), 
        								sqlQueryObjectInnerList, jdbcObjects, returnClassTypes, union, unionExpression);
        if(list!=null && list.size()>0){
			return list;
		}
		else{
			throw new NotFoundException("Not Found");
		}								
	}
	
	@Override
	public NonNegativeNumber unionCount(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
												Union union, UnionExpression ... unionExpression) throws ServiceException,NotFoundException,NotImplementedException,Exception {		
		
		List<ISQLQueryObject> sqlQueryObjectInnerList = new ArrayList<ISQLQueryObject>();
		List<JDBCObject> jdbcObjects = new ArrayList<JDBCObject>();
		List<Class<?>> returnClassTypes = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareUnionCount(jdbcProperties, log, connection, sqlQueryObject, 
        						this.getVistaRiscossioniFieldConverter(), VistaRiscossioni.model(), 
        						sqlQueryObjectInnerList, jdbcObjects, union, unionExpression);
		
		if(unionExpression!=null){
			for (int i = 0; i < unionExpression.length; i++) {
				UnionExpression ue = unionExpression[i];
				IExpression expression = ue.getExpression();
				_join(expression,sqlQueryObjectInnerList.get(i));
			}
		}
        
        NonNegativeNumber number = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.unionCount(jdbcProperties, log, connection, sqlQueryObject, 
        								this.getVistaRiscossioniFieldConverter(), VistaRiscossioni.model(), 
        								sqlQueryObjectInnerList, jdbcObjects, returnClassTypes, union, unionExpression);
        if(number!=null && number.longValue()>=0){
			return number;
		}
		else{
			throw new NotFoundException("Not Found");
		}
	}



	// -- ConstructorExpression	

	@Override
	public JDBCExpression newExpression(Logger log) throws NotImplementedException, ServiceException {
		try{
			return new JDBCExpression(this.getVistaRiscossioniFieldConverter());
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}


	@Override
	public JDBCPaginatedExpression newPaginatedExpression(Logger log) throws NotImplementedException, ServiceException {
		try{
			return new JDBCPaginatedExpression(this.getVistaRiscossioniFieldConverter());
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}
	
	@Override
	public JDBCExpression toExpression(JDBCPaginatedExpression paginatedExpression, Logger log) throws NotImplementedException, ServiceException {
		try{
			return new JDBCExpression(paginatedExpression);
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}

	@Override
	public JDBCPaginatedExpression toPaginatedExpression(JDBCExpression expression, Logger log) throws NotImplementedException, ServiceException {
		try{
			return new JDBCPaginatedExpression(expression);
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}
	
	
	
	// -- DB

	@Override
	public void mappingTableIds(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdVistaRiscossione id, VistaRiscossioni obj) throws NotFoundException,NotImplementedException,ServiceException,Exception{
		_mappingTableIds(jdbcProperties,log,connection,sqlQueryObject,obj,
				this.get(jdbcProperties,log,connection,sqlQueryObject,id,null));
	}
	
	@Override
	public void mappingTableIds(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId, VistaRiscossioni obj) throws NotFoundException,NotImplementedException,ServiceException,Exception{
		_mappingTableIds(jdbcProperties,log,connection,sqlQueryObject,obj,
				this.get(jdbcProperties,log,connection,sqlQueryObject,tableId,null));
	}
	private void _mappingTableIds(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, VistaRiscossioni obj, VistaRiscossioni imgSaved) throws NotFoundException,NotImplementedException,ServiceException,Exception{
		if(imgSaved==null){
			return;
		}
		obj.setId(imgSaved.getId());

	}
	
	@Override
	public VistaRiscossioni get(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId, org.openspcoop2.generic_project.beans.IDMappingBehaviour idMappingResolutionBehaviour) throws NotFoundException, MultipleResultException, NotImplementedException, ServiceException, Exception {
		return this._get(jdbcProperties, log, connection, sqlQueryObject, Long.valueOf(tableId), idMappingResolutionBehaviour);
	}
	
	private VistaRiscossioni _get(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, Long tableId, org.openspcoop2.generic_project.beans.IDMappingBehaviour idMappingResolutionBehaviour) throws NotFoundException, MultipleResultException, NotImplementedException, ServiceException, Exception {
	
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities jdbcUtilities = 
					new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities(sqlQueryObject.getTipoDatabaseOpenSPCoop2(), log, connection);
		
		// default behaviour (id-mapping)
		if(idMappingResolutionBehaviour==null){
			idMappingResolutionBehaviour = org.openspcoop2.generic_project.beans.IDMappingBehaviour.valueOf("USE_TABLE_ID");
		}
		
		ISQLQueryObject sqlQueryObjectGet = sqlQueryObject.newSQLQueryObject();
				
		VistaRiscossioni vistaRiscossioni = new VistaRiscossioni();
		

		// Object vistaRiscossioni
		ISQLQueryObject sqlQueryObjectGet_vistaRiscossioni = sqlQueryObjectGet.newSQLQueryObject();
		sqlQueryObjectGet_vistaRiscossioni.setANDLogicOperator(true);
		sqlQueryObjectGet_vistaRiscossioni.addFromTable(this.getVistaRiscossioniFieldConverter().toTable(VistaRiscossioni.model()));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField("id");
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().COD_DOMINIO,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().IUV,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().IUR,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().COD_FLUSSO,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().FR_IUR,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().DATA_REGOLAMENTO,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().NUMERO_PAGAMENTI,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().IMPORTO_TOTALE_PAGAMENTI,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().IMPORTO_PAGATO,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().DATA,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().COD_SINGOLO_VERSAMENTO_ENTE,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().INDICE_DATI,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().COD_VERSAMENTO_ENTE,true));
		sqlQueryObjectGet_vistaRiscossioni.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().COD_APPLICAZIONE,true));
		sqlQueryObjectGet_vistaRiscossioni.addWhereCondition("id=?");

		// Get vistaRiscossioni
		vistaRiscossioni = (VistaRiscossioni) jdbcUtilities.executeQuerySingleResult(sqlQueryObjectGet_vistaRiscossioni.createSQLQuery(), jdbcProperties.isShowSql(), VistaRiscossioni.model(), this.getVistaRiscossioniFetch(),
			new JDBCObject(tableId,Long.class));



		
        return vistaRiscossioni;  
	
	} 
	
	@Override
	public boolean exists(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId) throws MultipleResultException, NotImplementedException, ServiceException, Exception {
		return this._exists(jdbcProperties, log, connection, sqlQueryObject, Long.valueOf(tableId));
	}
	
	private boolean _exists(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, Long tableId) throws MultipleResultException, NotImplementedException, ServiceException, Exception {
	
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities jdbcUtilities = 
					new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities(sqlQueryObject.getTipoDatabaseOpenSPCoop2(), log, connection);
				
		boolean existsVistaRiscossioni = false;

		sqlQueryObject = sqlQueryObject.newSQLQueryObject();
		sqlQueryObject.setANDLogicOperator(true);

		sqlQueryObject.addFromTable(this.getVistaRiscossioniFieldConverter().toTable(VistaRiscossioni.model()));
		sqlQueryObject.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().COD_DOMINIO,true));
		sqlQueryObject.addWhereCondition("id=?");


		// Exists vistaRiscossioni
		existsVistaRiscossioni = jdbcUtilities.exists(sqlQueryObject.createSQLQuery(), jdbcProperties.isShowSql(),
			new JDBCObject(tableId,Long.class));

		
        return existsVistaRiscossioni;
	
	}
	
	private void _join(IExpression expression, ISQLQueryObject sqlQueryObject) throws NotImplementedException, ServiceException, Exception{
	
		/* 
		 * TODO: implement code that implement the join condition
		*/
		/*
		if(expression.inUseModel(VistaRiscossioni.model().XXXX,false)){
			String tableName1 = this.getVistaRiscossioniFieldConverter().toAliasTable(VistaRiscossioni.model());
			String tableName2 = this.getVistaRiscossioniFieldConverter().toAliasTable(VistaRiscossioni.model().XXX);
			sqlQueryObject.addWhereCondition(tableName1+".id="+tableName2+".id_table1");
		}
		*/
		
		/* 
         * TODO: implementa il codice che aggiunge la condizione FROM Table per le condizioni di join di oggetti annidati dal secondo livello in poi 
         *       La addFromTable deve essere aggiunta solo se l'oggetto del livello precedente non viene utilizzato nella espressione 
         *		 altrimenti il metodo sopra 'toSqlForPreparedStatementWithFromCondition' si occupa gia' di aggiungerla
        */
        /*
        if(expression.inUseModel(VistaRiscossioni.model().LEVEL1.LEVEL2,false)){
			if(expression.inUseModel(VistaRiscossioni.model().LEVEL1,false)==false){
				sqlQueryObject.addFromTable(this.getVistaRiscossioniFieldConverter().toTable(VistaRiscossioni.model().LEVEL1));
			}
		}
		...
		if(expression.inUseModel(VistaRiscossioni.model()....LEVELN.LEVELN+1,false)){
			if(expression.inUseModel(VistaRiscossioni.model().LEVELN,false)==false){
				sqlQueryObject.addFromTable(this.getVistaRiscossioniFieldConverter().toTable(VistaRiscossioni.model().LEVELN));
			}
		}
		*/
		
		// Delete this line when you have implemented the join condition
		int throwNotImplemented = 1;
		if(throwNotImplemented==1){
		        throw new NotImplementedException("NotImplemented");
		}
		// Delete this line when you have implemented the join condition
        
	}
	
	protected java.util.List<Object> _getRootTablePrimaryKeyValues(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdVistaRiscossione id) throws NotFoundException, ServiceException, NotImplementedException, Exception{
	    // Identificativi
        java.util.List<Object> rootTableIdValues = new java.util.ArrayList<Object>();
        // TODO: Define the column values used to identify the primary key
		Long longId = this.findIdVistaRiscossioni(jdbcProperties, log, connection, sqlQueryObject.newSQLQueryObject(), id, true);
		rootTableIdValues.add(longId);
        
        // Delete this line when you have verified the method
		int throwNotImplemented = 1;
		if(throwNotImplemented==1){
		        throw new NotImplementedException("NotImplemented");
		}
		// Delete this line when you have verified the method
        
        return rootTableIdValues;
	}
	
	protected Map<String, List<IField>> _getMapTableToPKColumn() throws NotImplementedException, Exception{
	
		VistaRiscossioniFieldConverter converter = this.getVistaRiscossioniFieldConverter();
		Map<String, List<IField>> mapTableToPKColumn = new java.util.Hashtable<String, List<IField>>();
		UtilsTemplate<IField> utilities = new UtilsTemplate<IField>();

		// TODO: Define the columns used to identify the primary key
		//		  If a table doesn't have a primary key, don't add it to this map

		// VistaRiscossioni.model()
		mapTableToPKColumn.put(converter.toTable(VistaRiscossioni.model()),
			utilities.newList(
				new CustomField("id", Long.class, "id", converter.toTable(VistaRiscossioni.model()))
			));


        // Delete this line when you have verified the method
		int throwNotImplemented = 1;
		if(throwNotImplemented==1){
		        throw new NotImplementedException("NotImplemented");
		}
		// Delete this line when you have verified the method
        
        return mapTableToPKColumn;		
	}
	
	@Override
	public List<Long> findAllTableIds(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCPaginatedExpression paginatedExpression) throws ServiceException, NotImplementedException, Exception {
		
		List<Long> list = new ArrayList<Long>();

		sqlQueryObject.setSelectDistinct(true);
		sqlQueryObject.setANDLogicOperator(true);
		sqlQueryObject.addSelectField(this.getVistaRiscossioniFieldConverter().toTable(VistaRiscossioni.model())+".id");
		Class<?> objectIdClass = Long.class;
		
		List<Object> listaQuery = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareFindAll(jdbcProperties, log, connection, sqlQueryObject, paginatedExpression,
												this.getVistaRiscossioniFieldConverter(), VistaRiscossioni.model());
		
		_join(paginatedExpression,sqlQueryObject);
		
		List<Object> listObjects = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.findAll(jdbcProperties, log, connection, sqlQueryObject, paginatedExpression,
																			this.getVistaRiscossioniFieldConverter(), VistaRiscossioni.model(), objectIdClass, listaQuery);
		for(Object object: listObjects) {
			list.add((Long)object);
		}

        return list;
		
	}
	
	@Override
	public long findTableId(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCExpression expression) throws ServiceException, NotFoundException, MultipleResultException, NotImplementedException, Exception {
	
		sqlQueryObject.setSelectDistinct(true);
		sqlQueryObject.setANDLogicOperator(true);
		sqlQueryObject.addSelectField(this.getVistaRiscossioniFieldConverter().toTable(VistaRiscossioni.model())+".id");
		Class<?> objectIdClass = Long.class;
		
		List<Object> listaQuery = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareFind(jdbcProperties, log, connection, sqlQueryObject, expression,
												this.getVistaRiscossioniFieldConverter(), VistaRiscossioni.model());
		
		_join(expression,sqlQueryObject);

		Object res = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.find(jdbcProperties, log, connection, sqlQueryObject, expression,
														this.getVistaRiscossioniFieldConverter(), VistaRiscossioni.model(), objectIdClass, listaQuery);
		if(res!=null && (((Long) res).longValue()>0) ){
			return ((Long) res).longValue();
		}
		else{
			throw new NotFoundException("Not Found");
		}
		
	}

	@Override
	public InUse inUse(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId) throws ServiceException, NotFoundException, NotImplementedException, Exception {
		return this._inUse(jdbcProperties, log, connection, sqlQueryObject, Long.valueOf(tableId));
	}

	private InUse _inUse(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, Long tableId) throws ServiceException, NotFoundException, NotImplementedException, Exception {

		InUse inUse = new InUse();
		inUse.setInUse(false);
		
		/* 
		 * TODO: implement code that checks whether the object identified by the id parameter is used by other objects
		*/
		
		// Delete this line when you have implemented the method
		int throwNotImplemented = 1;
		if(throwNotImplemented==1){
		        throw new NotImplementedException("NotImplemented");
		}
		// Delete this line when you have implemented the method

        return inUse;

	}
	
	@Override
	public IdVistaRiscossione findId(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId, boolean throwNotFound)
			throws NotFoundException, ServiceException, NotImplementedException, Exception {
		
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities jdbcUtilities = 
			new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities(sqlQueryObject.getTipoDatabaseOpenSPCoop2(), log, connection);

		ISQLQueryObject sqlQueryObjectGet = sqlQueryObject.newSQLQueryObject();

		/* 
		 * TODO: implement code that returns the object identified by the id
		*/

		// Delete this line when you have implemented the method
		int throwNotImplemented = 1;
		if(throwNotImplemented==1){
		        throw new NotImplementedException("NotImplemented");
		}
 		// Delete this line when you have implemented the method                

		// Object _vistaRiscossioni
		//TODO Implementare la ricerca dell'id
		sqlQueryObjectGet.addFromTable(this.getVistaRiscossioniFieldConverter().toTable(VistaRiscossioni.model()));
		// TODO select field for identify ObjectId
		//sqlQueryObjectGet.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().NOME_COLONNA_1,true));
		//...
		//sqlQueryObjectGet.addSelectField(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().NOME_COLONNA_N,true));
		sqlQueryObjectGet.setANDLogicOperator(true);
		sqlQueryObjectGet.addWhereCondition("id=?");

		// Recupero _vistaRiscossioni
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject [] searchParams_vistaRiscossioni = new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject [] { 
			new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject(tableId,Long.class)
		};
		List<Class<?>> listaFieldIdReturnType_vistaRiscossioni = new ArrayList<Class<?>>();
		//listaFieldIdReturnType_vistaRiscossioni.add(Id1.class);
		//...
		//listaFieldIdReturnType_vistaRiscossioni.add(IdN.class);
		it.govpay.orm.IdVistaRiscossione id_vistaRiscossioni = null;
		List<Object> listaFieldId_vistaRiscossioni = jdbcUtilities.executeQuerySingleResult(sqlQueryObjectGet.createSQLQuery(), jdbcProperties.isShowSql(),
				listaFieldIdReturnType_vistaRiscossioni, searchParams_vistaRiscossioni);
		if(listaFieldId_vistaRiscossioni==null || listaFieldId_vistaRiscossioni.size()<=0){
			if(throwNotFound){
				throw new NotFoundException("Not Found");
			}
		}
		else{
			// set _vistaRiscossioni
			id_vistaRiscossioni = new it.govpay.orm.IdVistaRiscossione();
			// id_vistaRiscossioni.setId1(listaFieldId_vistaRiscossioni.get(0));
			// ...
			// id_vistaRiscossioni.setIdN(listaFieldId_vistaRiscossioni.get(N-1));
		}
		
		return id_vistaRiscossioni;
		
	}

	@Override
	public Long findTableId(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdVistaRiscossione id, boolean throwNotFound)
			throws NotFoundException, ServiceException, NotImplementedException, Exception {
	
		return this.findIdVistaRiscossioni(jdbcProperties,log,connection,sqlQueryObject,id,throwNotFound);
			
	}
	
	@Override
	public List<List<Object>> nativeQuery(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
											String sql,List<Class<?>> returnClassTypes,Object ... param) throws ServiceException,NotFoundException,NotImplementedException,Exception{
		
		return org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.nativeQuery(jdbcProperties, log, connection, sqlQueryObject,
																							sql,returnClassTypes,param);
														
	}
	
	protected Long findIdVistaRiscossioni(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdVistaRiscossione id, boolean throwNotFound) throws NotFoundException, ServiceException, NotImplementedException, Exception {

		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities jdbcUtilities = 
			new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities(sqlQueryObject.getTipoDatabaseOpenSPCoop2(), log, connection);

		ISQLQueryObject sqlQueryObjectGet = sqlQueryObject.newSQLQueryObject();

		/* 
		 * TODO: implement code that returns the object identified by the id
		*/

		// Delete this line when you have implemented the method
		int throwNotImplemented = 1;
		if(throwNotImplemented==1){
		        throw new NotImplementedException("NotImplemented");
		}
 		// Delete this line when you have implemented the method                

		// Object _vistaRiscossioni
		//TODO Implementare la ricerca dell'id
		sqlQueryObjectGet.addFromTable(this.getVistaRiscossioniFieldConverter().toTable(VistaRiscossioni.model()));
		sqlQueryObjectGet.addSelectField("id");
		// Devono essere mappati nella where condition i metodi dell'oggetto id.getXXX
		sqlQueryObjectGet.setANDLogicOperator(true);
		sqlQueryObjectGet.setSelectDistinct(true);
		//sqlQueryObjectGet.addWhereCondition(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().NOME_COLONNA_1,true)+"=?");
		// ...
		//sqlQueryObjectGet.addWhereCondition(this.getVistaRiscossioniFieldConverter().toColumn(VistaRiscossioni.model().NOME_COLONNA_N,true)+"=?");

		// Recupero _vistaRiscossioni
		// TODO Aggiungere i valori dei parametri di ricerca sopra definiti recuperandoli con i metodi dell'oggetto id.getXXX
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject [] searchParams_vistaRiscossioni = new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject [] { 
			//new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject(object,object.class),
			//...
			//new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject(object,object.class)
		};
		Long id_vistaRiscossioni = null;
		try{
			id_vistaRiscossioni = (Long) jdbcUtilities.executeQuerySingleResult(sqlQueryObjectGet.createSQLQuery(), jdbcProperties.isShowSql(),
						Long.class, searchParams_vistaRiscossioni);
		}catch(NotFoundException notFound){
			if(throwNotFound){
				throw new NotFoundException(notFound);
			}
		}
		if(id_vistaRiscossioni==null || id_vistaRiscossioni<=0){
			if(throwNotFound){
				throw new NotFoundException("Not Found");
			}
		}
		
		return id_vistaRiscossioni;
	}
}