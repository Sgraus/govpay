/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2016 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import it.govpay.orm.Applicazione;
import it.govpay.orm.IdApplicazione;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.openspcoop2.generic_project.beans.NonNegativeNumber;
import org.openspcoop2.generic_project.beans.UpdateField;
import org.openspcoop2.generic_project.beans.UpdateModel;
import org.openspcoop2.generic_project.dao.jdbc.IJDBCServiceCRUDWithId;
import org.openspcoop2.generic_project.dao.jdbc.JDBCExpression;
import org.openspcoop2.generic_project.dao.jdbc.JDBCPaginatedExpression;
import org.openspcoop2.generic_project.dao.jdbc.JDBCServiceManagerProperties;
import org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject;
import org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.NotImplementedException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.IExpression;
import org.openspcoop2.utils.sql.ISQLQueryObject;

/**     
 * JDBCApplicazioneServiceImpl
 *
 * @author Giovanni Bussu (bussu@link.it)
 * @author Lorenzo Nardi (nardi@link.it)
 * @author $Author$
 * @version $Rev$, $Date$
 */
public class JDBCApplicazioneServiceImpl extends JDBCApplicazioneServiceSearchImpl
	implements IJDBCServiceCRUDWithId<Applicazione, IdApplicazione, JDBCServiceManager> {

	@Override
	public void create(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, Applicazione applicazione, org.openspcoop2.generic_project.beans.IDMappingBehaviour idMappingResolutionBehaviour) throws NotImplementedException,ServiceException,Exception {

		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities jdbcUtilities = 
				new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities(sqlQueryObject.getTipoDatabaseOpenSPCoop2(), log, connection);
		
		// default behaviour (id-mapping)
		if(idMappingResolutionBehaviour==null){
			idMappingResolutionBehaviour = org.openspcoop2.generic_project.beans.IDMappingBehaviour.valueOf("USE_TABLE_ID");
		}
		
		ISQLQueryObject sqlQueryObjectInsert = sqlQueryObject.newSQLQueryObject();
				


		// Object applicazione
		sqlQueryObjectInsert.addInsertTable(this.getApplicazioneFieldConverter().toTable(Applicazione.model()));
		sqlQueryObjectInsert.addInsertField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().COD_APPLICAZIONE,false),"?");
		sqlQueryObjectInsert.addInsertField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().ABILITATO,false),"?");
		sqlQueryObjectInsert.addInsertField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().PRINCIPAL,false),"?");
		sqlQueryObjectInsert.addInsertField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().FIRMA_RICEVUTA,false),"?");
		sqlQueryObjectInsert.addInsertField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().COD_CONNETTORE_ESITO,false),"?");
		sqlQueryObjectInsert.addInsertField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().COD_CONNETTORE_VERIFICA,false),"?");
		sqlQueryObjectInsert.addInsertField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().VERSIONE,false),"?");
		sqlQueryObjectInsert.addInsertField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().TRUSTED,false),"?");
		sqlQueryObjectInsert.addInsertField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().COD_APPLICAZIONE_IUV,false),"?");

		// Insert applicazione
		org.openspcoop2.utils.jdbc.IKeyGeneratorObject keyGenerator = this.getApplicazioneFetch().getKeyGeneratorObject(Applicazione.model());
		long id = jdbcUtilities.insertAndReturnGeneratedKey(sqlQueryObjectInsert, keyGenerator, jdbcProperties.isShowSql(),
			new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject(applicazione.getCodApplicazione(),Applicazione.model().COD_APPLICAZIONE.getFieldType()),
			new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject(applicazione.getAbilitato(),Applicazione.model().ABILITATO.getFieldType()),
			new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject(applicazione.getPrincipal(),Applicazione.model().PRINCIPAL.getFieldType()),
			new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject(applicazione.getFirmaRicevuta(),Applicazione.model().FIRMA_RICEVUTA.getFieldType()),
			new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject(applicazione.getCodConnettoreEsito(),Applicazione.model().COD_CONNETTORE_ESITO.getFieldType()),
			new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject(applicazione.getCodConnettoreVerifica(),Applicazione.model().COD_CONNETTORE_VERIFICA.getFieldType()),
			new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject(applicazione.getVersione(),Applicazione.model().VERSIONE.getFieldType()),
			new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject(applicazione.getTrusted(),Applicazione.model().TRUSTED.getFieldType()),
			new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject(applicazione.getCodApplicazioneIuv(),Applicazione.model().COD_APPLICAZIONE_IUV.getFieldType())
		);
		applicazione.setId(id);

		
	}

	@Override
	public void update(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdApplicazione oldId, Applicazione applicazione, org.openspcoop2.generic_project.beans.IDMappingBehaviour idMappingResolutionBehaviour) throws NotFoundException, NotImplementedException, ServiceException, Exception {
		ISQLQueryObject sqlQueryObjectUpdate = sqlQueryObject.newSQLQueryObject();
		Long longIdByLogicId = this.findIdApplicazione(jdbcProperties, log, connection, sqlQueryObjectUpdate.newSQLQueryObject(), oldId, true);
		Long tableId = applicazione.getId();
		if(tableId != null && tableId.longValue() > 0) {
			if(tableId.longValue() != longIdByLogicId.longValue()) {
				throw new Exception("Ambiguous parameter: applicazione.id ["+tableId+"] does not match logic id ["+longIdByLogicId+"]");
			}
		} else {
			tableId = longIdByLogicId;
			applicazione.setId(tableId);
		}
		if(tableId==null || tableId<=0){
			throw new Exception("Retrieve tableId failed");
		}

		this.update(jdbcProperties, log, connection, sqlQueryObject, tableId, applicazione, idMappingResolutionBehaviour);
	}
	@Override
	public void update(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId, Applicazione applicazione, org.openspcoop2.generic_project.beans.IDMappingBehaviour idMappingResolutionBehaviour) throws NotFoundException, NotImplementedException, ServiceException, Exception {
	
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities jdbcUtilities = 
				new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities(sqlQueryObject.getTipoDatabaseOpenSPCoop2(), log, connection);
		
		// default behaviour (id-mapping)
		if(idMappingResolutionBehaviour==null){
			idMappingResolutionBehaviour = org.openspcoop2.generic_project.beans.IDMappingBehaviour.valueOf("USE_TABLE_ID");
		}
		
		ISQLQueryObject sqlQueryObjectInsert = sqlQueryObject.newSQLQueryObject();
		ISQLQueryObject sqlQueryObjectDelete = sqlQueryObjectInsert.newSQLQueryObject();
		ISQLQueryObject sqlQueryObjectGet = sqlQueryObjectDelete.newSQLQueryObject();
		ISQLQueryObject sqlQueryObjectUpdate = sqlQueryObjectGet.newSQLQueryObject();
		
//		boolean setIdMappingResolutionBehaviour = 
//			(idMappingResolutionBehaviour==null) ||
//			org.openspcoop2.generic_project.beans.IDMappingBehaviour.ENABLED.equals(idMappingResolutionBehaviour) ||
//			org.openspcoop2.generic_project.beans.IDMappingBehaviour.USE_TABLE_ID.equals(idMappingResolutionBehaviour);
			


		// Object applicazione
		sqlQueryObjectUpdate.setANDLogicOperator(true);
		sqlQueryObjectUpdate.addUpdateTable(this.getApplicazioneFieldConverter().toTable(Applicazione.model()));
		boolean isUpdate_applicazione = true;
		java.util.List<JDBCObject> lstObjects_applicazione = new java.util.ArrayList<JDBCObject>();
		sqlQueryObjectUpdate.addUpdateField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().COD_APPLICAZIONE,false), "?");
		lstObjects_applicazione.add(new JDBCObject(applicazione.getCodApplicazione(), Applicazione.model().COD_APPLICAZIONE.getFieldType()));
		sqlQueryObjectUpdate.addUpdateField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().ABILITATO,false), "?");
		lstObjects_applicazione.add(new JDBCObject(applicazione.getAbilitato(), Applicazione.model().ABILITATO.getFieldType()));
		sqlQueryObjectUpdate.addUpdateField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().PRINCIPAL,false), "?");
		lstObjects_applicazione.add(new JDBCObject(applicazione.getPrincipal(), Applicazione.model().PRINCIPAL.getFieldType()));
		sqlQueryObjectUpdate.addUpdateField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().FIRMA_RICEVUTA,false), "?");
		lstObjects_applicazione.add(new JDBCObject(applicazione.getFirmaRicevuta(), Applicazione.model().FIRMA_RICEVUTA.getFieldType()));
		sqlQueryObjectUpdate.addUpdateField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().COD_CONNETTORE_ESITO,false), "?");
		lstObjects_applicazione.add(new JDBCObject(applicazione.getCodConnettoreEsito(), Applicazione.model().COD_CONNETTORE_ESITO.getFieldType()));
		sqlQueryObjectUpdate.addUpdateField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().COD_CONNETTORE_VERIFICA,false), "?");
		lstObjects_applicazione.add(new JDBCObject(applicazione.getCodConnettoreVerifica(), Applicazione.model().COD_CONNETTORE_VERIFICA.getFieldType()));
		sqlQueryObjectUpdate.addUpdateField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().VERSIONE,false), "?");
		lstObjects_applicazione.add(new JDBCObject(applicazione.getVersione(), Applicazione.model().VERSIONE.getFieldType()));
		sqlQueryObjectUpdate.addUpdateField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().TRUSTED,false), "?");
		lstObjects_applicazione.add(new JDBCObject(applicazione.getTrusted(), Applicazione.model().TRUSTED.getFieldType()));
		sqlQueryObjectUpdate.addUpdateField(this.getApplicazioneFieldConverter().toColumn(Applicazione.model().COD_APPLICAZIONE_IUV,false), "?");
		lstObjects_applicazione.add(new JDBCObject(applicazione.getCodApplicazioneIuv(), Applicazione.model().COD_APPLICAZIONE_IUV.getFieldType()));
		sqlQueryObjectUpdate.addWhereCondition("id=?");
		lstObjects_applicazione.add(new JDBCObject(tableId, Long.class));

		if(isUpdate_applicazione) {
			// Update applicazione
			jdbcUtilities.executeUpdate(sqlQueryObjectUpdate.createSQLUpdate(), jdbcProperties.isShowSql(), 
				lstObjects_applicazione.toArray(new JDBCObject[]{}));
		}


	}
	
	@Override
	public void updateFields(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdApplicazione id, UpdateField ... updateFields) throws NotFoundException, NotImplementedException, ServiceException, Exception {
		
		JDBCUtilities.updateFields(jdbcProperties, log, connection, sqlQueryObject, 
				this.getApplicazioneFieldConverter().toTable(Applicazione.model()), 
				this._getMapTableToPKColumn(), 
				this._getRootTablePrimaryKeyValues(jdbcProperties, log, connection, sqlQueryObject, id),
				this.getApplicazioneFieldConverter(), this, null, updateFields);
	}
	
	@Override
	public void updateFields(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdApplicazione id, IExpression condition, UpdateField ... updateFields) throws NotFoundException, NotImplementedException, ServiceException, Exception {
		
		JDBCUtilities.updateFields(jdbcProperties, log, connection, sqlQueryObject, 
				this.getApplicazioneFieldConverter().toTable(Applicazione.model()), 
				this._getMapTableToPKColumn(), 
				this._getRootTablePrimaryKeyValues(jdbcProperties, log, connection, sqlQueryObject, id),
				this.getApplicazioneFieldConverter(), this, condition, updateFields);
	}
	
	@Override
	public void updateFields(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdApplicazione id, UpdateModel ... updateModels) throws NotFoundException, NotImplementedException, ServiceException, Exception {
		
		JDBCUtilities.updateFields(jdbcProperties, log, connection, sqlQueryObject, 
				this.getApplicazioneFieldConverter().toTable(Applicazione.model()), 
				this._getMapTableToPKColumn(), 
				this._getRootTablePrimaryKeyValues(jdbcProperties, log, connection, sqlQueryObject, id),
				this.getApplicazioneFieldConverter(), this, updateModels);
	}	
	
	@Override
	public void updateFields(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId, UpdateField ... updateFields) throws NotFoundException, NotImplementedException, ServiceException, Exception {
		java.util.List<Object> ids = new java.util.ArrayList<Object>();
		ids.add(tableId);
		JDBCUtilities.updateFields(jdbcProperties, log, connection, sqlQueryObject, 
				this.getApplicazioneFieldConverter().toTable(Applicazione.model()), 
				this._getMapTableToPKColumn(), 
				ids,
				this.getApplicazioneFieldConverter(), this, null, updateFields);
	}
	
	@Override
	public void updateFields(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId, IExpression condition, UpdateField ... updateFields) throws NotFoundException, NotImplementedException, ServiceException, Exception {
		java.util.List<Object> ids = new java.util.ArrayList<Object>();
		ids.add(tableId);
		JDBCUtilities.updateFields(jdbcProperties, log, connection, sqlQueryObject, 
				this.getApplicazioneFieldConverter().toTable(Applicazione.model()), 
				this._getMapTableToPKColumn(), 
				ids,
				this.getApplicazioneFieldConverter(), this, condition, updateFields);
	}
	
	@Override
	public void updateFields(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId, UpdateModel ... updateModels) throws NotFoundException, NotImplementedException, ServiceException, Exception {
		java.util.List<Object> ids = new java.util.ArrayList<Object>();
		ids.add(tableId);
		JDBCUtilities.updateFields(jdbcProperties, log, connection, sqlQueryObject, 
				this.getApplicazioneFieldConverter().toTable(Applicazione.model()), 
				this._getMapTableToPKColumn(), 
				ids,
				this.getApplicazioneFieldConverter(), this, updateModels);
	}
	
	@Override
	public void updateOrCreate(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdApplicazione oldId, Applicazione applicazione, org.openspcoop2.generic_project.beans.IDMappingBehaviour idMappingResolutionBehaviour) throws NotImplementedException,ServiceException,Exception {
	
		// default behaviour (id-mapping)
		if(idMappingResolutionBehaviour==null){
			idMappingResolutionBehaviour = org.openspcoop2.generic_project.beans.IDMappingBehaviour.valueOf("USE_TABLE_ID");
		}
		
		if(this.exists(jdbcProperties, log, connection, sqlQueryObject, oldId)) {
			this.update(jdbcProperties, log, connection, sqlQueryObject, oldId, applicazione,idMappingResolutionBehaviour);
		} else {
			this.create(jdbcProperties, log, connection, sqlQueryObject, applicazione,idMappingResolutionBehaviour);
		}
		
	}
	
	@Override
	public void updateOrCreate(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId, Applicazione applicazione, org.openspcoop2.generic_project.beans.IDMappingBehaviour idMappingResolutionBehaviour) throws NotImplementedException,ServiceException,Exception {
		// default behaviour (id-mapping)
		if(idMappingResolutionBehaviour==null){
			idMappingResolutionBehaviour = org.openspcoop2.generic_project.beans.IDMappingBehaviour.valueOf("USE_TABLE_ID");
		}
		
		if(this.exists(jdbcProperties, log, connection, sqlQueryObject, tableId)) {
			this.update(jdbcProperties, log, connection, sqlQueryObject, tableId, applicazione,idMappingResolutionBehaviour);
		} else {
			this.create(jdbcProperties, log, connection, sqlQueryObject, applicazione,idMappingResolutionBehaviour);
		}
	}
	
	@Override
	public void delete(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, Applicazione applicazione) throws NotImplementedException,ServiceException,Exception {
		
		
		Long longId = null;
		if( (applicazione.getId()!=null) && (applicazione.getId()>0) ){
			longId = applicazione.getId();
		}
		else{
			IdApplicazione idApplicazione = this.convertToId(jdbcProperties,log,connection,sqlQueryObject,applicazione);
			longId = this.findIdApplicazione(jdbcProperties,log,connection,sqlQueryObject,idApplicazione,false);
			if(longId == null){
				return; // entry not exists
			}
		}		
		
		this._delete(jdbcProperties, log, connection, sqlQueryObject, longId);
		
	}

	private void _delete(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, Long id) throws NotImplementedException,ServiceException,Exception {
	
		if(id!=null && id.longValue()<=0){
			throw new ServiceException("Id is less equals 0");
		}
		
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities jdbcUtilities = 
				new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities(sqlQueryObject.getTipoDatabaseOpenSPCoop2(), log, connection);
		
		ISQLQueryObject sqlQueryObjectDelete = sqlQueryObject.newSQLQueryObject();
		

		// Object applicazione
		sqlQueryObjectDelete.setANDLogicOperator(true);
		sqlQueryObjectDelete.addDeleteTable(this.getApplicazioneFieldConverter().toTable(Applicazione.model()));
		if(id != null)
			sqlQueryObjectDelete.addWhereCondition("id=?");

		// Delete applicazione
		jdbcUtilities.execute(sqlQueryObjectDelete.createSQLDelete(), jdbcProperties.isShowSql(), 
			new JDBCObject(id,Long.class));

	}

	@Override
	public void deleteById(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, IdApplicazione idApplicazione) throws NotImplementedException,ServiceException,Exception {

		Long id = null;
		try{
			id = this.findIdApplicazione(jdbcProperties, log, connection, sqlQueryObject, idApplicazione, true);
		}catch(NotFoundException notFound){
			return;
		}
		this._delete(jdbcProperties, log, connection, sqlQueryObject, id);
		
	}
	
	@Override
	public NonNegativeNumber deleteAll(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject) throws NotImplementedException,ServiceException,Exception {
		
		return this.deleteAll(jdbcProperties, log, connection, sqlQueryObject, new JDBCExpression(this.getApplicazioneFieldConverter()));

	}

	@Override
	public NonNegativeNumber deleteAll(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCExpression expression) throws NotImplementedException, ServiceException,Exception {

		java.util.List<Long> lst = this.findAllTableIds(jdbcProperties, log, connection, sqlQueryObject, new JDBCPaginatedExpression(expression));
		
		for(Long id : lst) {
			this._delete(jdbcProperties, log, connection, sqlQueryObject, id);
		}
		
		return new NonNegativeNumber(lst.size());
	
	}



	// -- DB
	
	@Override
	public void deleteById(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId) throws ServiceException, NotImplementedException, Exception {
		this._delete(jdbcProperties, log, connection, sqlQueryObject, Long.valueOf(tableId));
	}
}
