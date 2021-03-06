package it.govpay.rs.v1.controllers.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.BodyPart;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.mime.MimeMultipart;
import org.openspcoop2.utils.serialization.SerializationConfig;
import org.slf4j.Logger;

import it.govpay.bd.model.Operazione;
import it.govpay.bd.model.Tracciato;
import it.govpay.core.dao.commons.exception.NonTrovataException;
import it.govpay.core.dao.pagamenti.PendenzeDAO;
import it.govpay.core.dao.pagamenti.TracciatiDAO;
import it.govpay.core.dao.pagamenti.dto.LeggiPendenzaDTO;
import it.govpay.core.dao.pagamenti.dto.LeggiPendenzaDTOResponse;
import it.govpay.core.dao.pagamenti.dto.LeggiTracciatoDTO;
import it.govpay.core.dao.pagamenti.dto.ListaOperazioniTracciatoDTO;
import it.govpay.core.dao.pagamenti.dto.ListaOperazioniTracciatoDTOResponse;
import it.govpay.core.dao.pagamenti.dto.ListaPendenzeDTO;
import it.govpay.core.dao.pagamenti.dto.ListaPendenzeDTOResponse;
import it.govpay.core.dao.pagamenti.dto.ListaTracciatiDTO;
import it.govpay.core.dao.pagamenti.dto.ListaTracciatiDTOResponse;
import it.govpay.core.dao.pagamenti.dto.PatchPendenzaDTO;
import it.govpay.core.dao.pagamenti.dto.PostTracciatoDTO;
import it.govpay.core.dao.pagamenti.exception.PendenzaNonTrovataException;
import it.govpay.core.exceptions.GovPayException;
import it.govpay.core.exceptions.NotAuthenticatedException;
import it.govpay.core.exceptions.NotAuthorizedException;
import it.govpay.core.rs.v1.beans.JSONSerializable;
import it.govpay.core.rs.v1.beans.base.Avviso;
import it.govpay.core.rs.v1.beans.base.DettaglioTracciatoPendenzeEsito;
import it.govpay.core.rs.v1.beans.base.EsitoOperazionePendenza;
import it.govpay.core.rs.v1.beans.base.FaultBean;
import it.govpay.core.rs.v1.beans.base.FaultBean.CategoriaEnum;
import it.govpay.core.rs.v1.beans.base.ListaOperazioniPendenza;
import it.govpay.core.rs.v1.beans.base.ListaPendenze;
import it.govpay.core.rs.v1.beans.base.ListaTracciatiPendenza;
import it.govpay.core.rs.v1.beans.base.OperazionePendenza;
import it.govpay.core.rs.v1.beans.base.PatchOp;
import it.govpay.core.rs.v1.beans.base.PatchOp.OpEnum;
import it.govpay.core.rs.v1.beans.base.Pendenza;
import it.govpay.core.rs.v1.beans.base.PendenzaIndex;
import it.govpay.core.rs.v1.beans.base.StatoOperazionePendenza;
import it.govpay.core.rs.v1.beans.base.StatoTracciatoPendenza;
import it.govpay.core.rs.v1.beans.base.TracciatoPendenze;
import it.govpay.core.rs.v1.beans.base.TracciatoPendenzeEsito;
import it.govpay.core.rs.v1.beans.base.TracciatoPendenzePost;
import it.govpay.core.utils.GovpayConfig;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.core.utils.SimpleDateFormatUtils;
import it.govpay.model.IAutorizzato;
import it.govpay.model.Tracciato.STATO_ELABORAZIONE;
import it.govpay.model.Tracciato.TIPO_TRACCIATO;
import it.govpay.rs.BaseRsService;
import it.govpay.rs.v1.beans.converter.PendenzeConverter;
import it.govpay.rs.v1.beans.converter.TracciatiConverter;


public class PendenzeController extends it.govpay.rs.BaseController {

	private SerializationConfig serializationConfig  = null;

	public PendenzeController(String nomeServizio,Logger log) {
		super(nomeServizio,log, GovpayConfig.GOVPAY_BACKOFFICE_OPEN_API_FILE_NAME);

		this.serializationConfig = new SerializationConfig();
		this.serializationConfig.setExcludes(Arrays.asList("jsonIdFilter"));
		this.serializationConfig.setDf(SimpleDateFormatUtils.newSimpleDateFormatDataOreMinuti());
	}

	public Response pendenzeIdA2AIdPendenzaGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , String idA2A, String idPendenza) {
		return pendenzeIdA2AIdPendenzaGET(user, uriInfo, httpHeaders, idA2A, idPendenza, false);
	}

	public Response pendenzeIdA2AIdPendenzaGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , String idA2A, String idPendenza, boolean addInfoIncasso) {
		String methodName = "getByIda2aIdPendenza";  
		GpContext ctx = null;
		String transactionId = null;
		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 

		try(ByteArrayOutputStream baos= new ByteArrayOutputStream();){
			this.logRequest(uriInfo, httpHeaders, methodName, baos);

			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();

			LeggiPendenzaDTO leggiPendenzaDTO = new LeggiPendenzaDTO(user);

			leggiPendenzaDTO.setInfoIncasso(addInfoIncasso);
			leggiPendenzaDTO.setCodA2A(idA2A);
			leggiPendenzaDTO.setCodPendenza(idPendenza);

			PendenzeDAO pendenzeDAO = new PendenzeDAO(); 

			LeggiPendenzaDTOResponse ricevutaDTOResponse = pendenzeDAO.leggiPendenza(leggiPendenzaDTO);

			Pendenza pendenza = addInfoIncasso ? 
					PendenzeConverter.toRsModelConInfoIncasso(ricevutaDTOResponse.getVersamentoIncasso(), ricevutaDTOResponse.getUnitaOperativa(), ricevutaDTOResponse.getApplicazione(), ricevutaDTOResponse.getDominio(), ricevutaDTOResponse.getLstSingoliVersamenti())
					: PendenzeConverter.toRsModel(ricevutaDTOResponse.getVersamento(), ricevutaDTOResponse.getUnitaOperativa(), ricevutaDTOResponse.getApplicazione(), ricevutaDTOResponse.getDominio(), ricevutaDTOResponse.getLstSingoliVersamenti());
					this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
					return this.handleResponseOk(Response.status(Status.OK).entity(pendenza.toJSON(null)),transactionId).build();
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
	}

	public Response pendenzeGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer pagina, Integer risultatiPerPagina, String ordinamento, String campi, String idDominio, String idA2A, String idDebitore, String stato, String idPagamento) {
		return pendenzeGET(user, uriInfo, httpHeaders, pagina, risultatiPerPagina, ordinamento, campi, idDominio, idA2A, idDebitore, stato, idPagamento, false);
	}

	public Response pendenzeGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer pagina, Integer risultatiPerPagina, String ordinamento, String campi, String idDominio, String idA2A, String idDebitore, String stato, String idPagamento, boolean addInfoIncasso) {
		GpContext ctx = null;
		String transactionId = null;

		String methodName = "pendenzeGET";
		try(ByteArrayOutputStream baos= new ByteArrayOutputStream();){
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 

			this.logRequest(uriInfo, httpHeaders, methodName, baos);

			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();

			// Parametri - > DTO Input

			ListaPendenzeDTO listaPendenzeDTO = new ListaPendenzeDTO(user,addInfoIncasso);

			listaPendenzeDTO.setPagina(pagina);
			listaPendenzeDTO.setLimit(risultatiPerPagina);
			listaPendenzeDTO.setStato(stato);

			if(idDominio != null)
				listaPendenzeDTO.setIdDominio(idDominio);
			if(idA2A != null)
				listaPendenzeDTO.setIdA2A(idA2A);
			if(idDebitore != null)
				listaPendenzeDTO.setIdDebitore(idDebitore);

			if(idPagamento != null)
				listaPendenzeDTO.setIdPagamento(idPagamento);

			if(ordinamento != null)
				listaPendenzeDTO.setOrderBy(ordinamento);
			// INIT DAO

			PendenzeDAO pendenzeDAO = new PendenzeDAO(); 

			listaPendenzeDTO.setInfoIncasso(addInfoIncasso); 

			// CHIAMATA AL DAO

			ListaPendenzeDTOResponse listaPendenzeDTOResponse = pendenzeDAO.listaPendenze(listaPendenzeDTO);

			// CONVERT TO JSON DELLA RISPOSTA

			List<PendenzaIndex> results = new ArrayList<>();
			for(LeggiPendenzaDTOResponse ricevutaDTOResponse: listaPendenzeDTOResponse.getResults()) {
				PendenzaIndex rsModel = addInfoIncasso ? PendenzeConverter.toRsModelIndexConInfoIncasso(ricevutaDTOResponse.getVersamentoIncasso()) : PendenzeConverter.toRsModelIndex(ricevutaDTOResponse.getVersamento()); 
				results.add(rsModel);
			}

			ListaPendenze response = new ListaPendenze(results, this.getServicePath(uriInfo),
					listaPendenzeDTOResponse.getTotalResults(), pagina, risultatiPerPagina);

			this.logResponse(uriInfo, httpHeaders, methodName, response.toJSON(campi), 200);
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(Status.OK).entity(response.toJSON(campi)),transactionId).build();

		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
	}

	public Response pendenzeIdA2AIdPendenzaPATCH(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , String idA2A, String idPendenza, java.io.InputStream is) {
		return pendenzeIdA2AIdPendenzaPATCH(user, uriInfo, httpHeaders, idA2A, idPendenza, is, false);
	}

	@SuppressWarnings("unchecked")
	public Response pendenzeIdA2AIdPendenzaPATCH(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , String idA2A, String idPendenza, java.io.InputStream is, boolean addInfoIncasso) {
		String methodName = "pendenzeIdA2AIdPendenzaPATCH";  
		GpContext ctx = null;
		String transactionId = null;

		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 
		try(ByteArrayOutputStream baos= new ByteArrayOutputStream();){

			// salvo il json ricevuto
			BaseRsService.copy(is, baos);
			this.logRequest(uriInfo, httpHeaders, methodName, baos);

			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();

			PendenzeDAO pendenzeDAO = new PendenzeDAO(); 

			PatchPendenzaDTO patchPendenzaDTO = new PatchPendenzaDTO(user);
			patchPendenzaDTO.setIdA2a(idA2A);
			patchPendenzaDTO.setIdPendenza(idPendenza);

			String jsonRequest = baos.toString();

			List<PatchOp> lstOp = new ArrayList<>();

			try {
				List<java.util.LinkedHashMap<?,?>> lst = JSONSerializable.parse(jsonRequest, List.class);
				for(java.util.LinkedHashMap<?,?> map: lst) {
					PatchOp op = new PatchOp();
					op.setOp(OpEnum.fromValue((String) map.get("op")));
					op.setPath((String) map.get("path"));
					op.setValue(map.get("value"));
					op.validate();
					lstOp.add(op);
				}
			} catch (ServiceException e) {
				lstOp = JSONSerializable.parse(jsonRequest, List.class);
				//				PatchOp op = PatchOp.parse(jsonRequest);
				//				op.validate();
				//				lstOp.add(op);
			}

			patchPendenzaDTO.setOp(lstOp );

			pendenzeDAO.patch(patchPendenzaDTO);

			LeggiPendenzaDTO leggiPendenzaDTO = new LeggiPendenzaDTO(user);

			leggiPendenzaDTO.setCodA2A(idA2A);
			leggiPendenzaDTO.setCodPendenza(idPendenza);
			leggiPendenzaDTO.setInfoIncasso(addInfoIncasso);

			LeggiPendenzaDTOResponse ricevutaDTOResponse = pendenzeDAO.leggiPendenza(leggiPendenzaDTO);

			Pendenza pendenza = 
					addInfoIncasso ? 
							PendenzeConverter.toRsModelConInfoIncasso(ricevutaDTOResponse.getVersamentoIncasso(), ricevutaDTOResponse.getUnitaOperativa(), ricevutaDTOResponse.getApplicazione(), ricevutaDTOResponse.getDominio(), ricevutaDTOResponse.getLstSingoliVersamenti())
							: PendenzeConverter.toRsModel(ricevutaDTOResponse.getVersamento(), ricevutaDTOResponse.getUnitaOperativa(), ricevutaDTOResponse.getApplicazione(), ricevutaDTOResponse.getDominio(), ricevutaDTOResponse.getLstSingoliVersamenti());
							this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
							return this.handleResponseOk(Response.status(Status.OK).entity(pendenza.toJSON(null)),transactionId).build();
		} catch(GovPayException e) {
			this.log.error("Errore durante il processo di pagamento", e);
			FaultBean respKo = new FaultBean();
			respKo.setCategoria(CategoriaEnum.OPERAZIONE);
			respKo.setCodice(e.getCodEsito().name());
			respKo.setDescrizione(e.getDescrizioneEsito());
			respKo.setDettaglio(e.getMessage());
			try {
				this.logResponse(uriInfo, httpHeaders, methodName, respKo.toJSON(null), 500);
			}catch(Exception e1) {
				this.log.error("Errore durante il log della risposta", e1);
			}
			return this.handleResponseOk(Response.status(Status.INTERNAL_SERVER_ERROR).entity(this.getRespJson(respKo)),transactionId).build();
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
	}



	public Response pendenzePOST(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , java.io.InputStream is) {
		String methodName = "pendenzePOST";  
		GpContext ctx = null;
		String transactionId = null;

		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 

		try(ByteArrayOutputStream baos= new ByteArrayOutputStream();){

			String contentTypeBody = null;
			if(httpHeaders.getRequestHeaders().containsKey("Content-Type")) {
				contentTypeBody = httpHeaders.getRequestHeaders().get("Content-Type").get(0);
			}

			this.log.info(MessageFormat.format("Content-Type della richiesta: {0}.", contentTypeBody));


			String fileName = null;
			InputStream fileInputStream = null;
			try{
				// controllo se sono in una richiesta multipart
				if(contentTypeBody != null && contentTypeBody.startsWith("multipart")) {
					javax.mail.internet.ContentType cType = new javax.mail.internet.ContentType(contentTypeBody);
					this.log.info(MessageFormat.format("Content-Type Boundary: [{0}]", cType.getParameter("boundary")));

					MimeMultipart mimeMultipart = new MimeMultipart(is,contentTypeBody);

					for(int i = 0 ; i < mimeMultipart.countBodyParts() ;  i ++) {
						BodyPart bodyPart = mimeMultipart.getBodyPart(i);
						fileName = getBodyPartFileName(bodyPart);

						if(fileName != null) {
							fileInputStream = bodyPart.getInputStream();
							break;
						}
					}

					if(fileInputStream != null) {
						BaseRsService.copy(fileInputStream, baos);
					}
				}
			}catch(Exception e) {
				this.log.error(e.getMessage(),e);
			}

			if(fileInputStream == null) {
				// salvo il json ricevuto
				BaseRsService.copy(is, baos);
			}

			this.logRequest(uriInfo, httpHeaders, methodName, baos);

			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();

			String jsonRequest = baos.toString();
			TracciatoPendenzePost tracciatoPendenzeRequest = JSONSerializable.parse(jsonRequest, TracciatoPendenzePost.class);

			tracciatoPendenzeRequest.validate();

			TracciatiDAO tracciatiDAO = new TracciatiDAO();

			PostTracciatoDTO postTracciatoDTO = new PostTracciatoDTO(user);

			postTracciatoDTO.setIdDominio(tracciatoPendenzeRequest.getIdDominio());
			postTracciatoDTO.setNomeFile(tracciatoPendenzeRequest.getIdTracciato());
			postTracciatoDTO.setContenuto(baos.toByteArray()); 

			tracciatiDAO.create(postTracciatoDTO);

			Status responseStatus = Status.OK;

			this.logResponse(uriInfo, httpHeaders, methodName, new byte[0], responseStatus.getStatusCode());
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(responseStatus),transactionId).build();
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
	}



	public Response pendenzeTracciatiGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer pagina, Integer risultatiPerPagina, String idDominio, StatoTracciatoPendenza stato) {
		GpContext ctx = null;
		String transactionId = null;

		String methodName = "pendenzeTracciatiGET";
		try(ByteArrayOutputStream baos= new ByteArrayOutputStream();){
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 

			this.logRequest(uriInfo, httpHeaders, methodName, baos);

			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();

			// Parametri - > DTO Input

			ListaTracciatiDTO listaTracciatiDTO = new ListaTracciatiDTO(user);

			listaTracciatiDTO.setPagina(pagina);
			listaTracciatiDTO.setLimit(risultatiPerPagina);
			listaTracciatiDTO.setStatoTracciatoPendenza(stato);
			List<TIPO_TRACCIATO> tipoTracciato = new ArrayList<>();
			tipoTracciato.add(TIPO_TRACCIATO.PENDENZA);
			listaTracciatiDTO.setTipoTracciato(tipoTracciato);
			listaTracciatiDTO.setIdDominio(idDominio);

			// INIT DAO

			TracciatiDAO tracciatiDAO = new TracciatiDAO(); 

			// CHIAMATA AL DAO

			ListaTracciatiDTOResponse listaTracciatiDTOResponse = tracciatiDAO.listaTracciati(listaTracciatiDTO);

			// CONVERT TO JSON DELLA RISPOSTA

			List<TracciatoPendenze> results = new ArrayList<>();
			for(Tracciato tracciato: listaTracciatiDTOResponse.getResults()) {
				TracciatoPendenze rsModel = TracciatiConverter.toTracciatoPendenzeRsModel(tracciato);
				results.add(rsModel);
			}

			ListaTracciatiPendenza response = new ListaTracciatiPendenza(results, this.getServicePath(uriInfo),
					listaTracciatiDTOResponse.getTotalResults(), pagina, risultatiPerPagina);

			this.logResponse(uriInfo, httpHeaders, methodName, response.toJSON(null,this.serializationConfig), 200);
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(Status.OK).entity(response.toJSON(null,this.serializationConfig)),transactionId).build();

		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
	}



	public Response pendenzeTracciatiIdEsitoGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer id) {
		String methodName = "pendenzeTracciatiIdEsitoGET";  
		GpContext ctx = null;
		String transactionId = null;

		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 

		try(ByteArrayOutputStream baos= new ByteArrayOutputStream();){

			this.logRequest(uriInfo, httpHeaders, methodName, baos);

			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();

			LeggiTracciatoDTO leggiTracciatoDTO = new LeggiTracciatoDTO(user);
			leggiTracciatoDTO.setId((long) id);

			TracciatiDAO tracciatiDAO = new TracciatiDAO();
			Tracciato tracciato = tracciatiDAO.leggiTracciato(leggiTracciatoDTO);

			if(tracciato.getStato().equals(STATO_ELABORAZIONE.ELABORAZIONE))
				throw new NonTrovataException("Tracciato di Esito non presente: elaborazione ancora in corso");

			TracciatoPendenzeEsito rsModel = TracciatiConverter.toTracciatoPendenzeEsitoRsModel(tracciato);
			this.logResponse(uriInfo, httpHeaders, methodName, rsModel.toJSON(null,this.serializationConfig), 200);
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(Status.OK).entity(rsModel.toJSON(null,this.serializationConfig)),transactionId).build();
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
	}



	public Response pendenzeTracciatiIdGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer id) {
		String methodName = "pendenzeTracciatiIdGET";  
		GpContext ctx = null;
		String transactionId = null;

		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 

		try(ByteArrayOutputStream baos= new ByteArrayOutputStream();){

			this.logRequest(uriInfo, httpHeaders, methodName, baos);

			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();

			LeggiTracciatoDTO leggiTracciatoDTO = new LeggiTracciatoDTO(user);
			leggiTracciatoDTO.setId((long) id);

			TracciatiDAO tracciatiDAO = new TracciatiDAO();
			Tracciato tracciato = tracciatiDAO.leggiTracciato(leggiTracciatoDTO);

			TracciatoPendenze rsModel = TracciatiConverter.toTracciatoPendenzeRsModel(tracciato);
			this.logResponse(uriInfo, httpHeaders, methodName, rsModel.toJSON(null,this.serializationConfig), 200);
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(Status.OK).entity(rsModel.toJSON(null,this.serializationConfig)),transactionId).build();
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
	}



	public Response pendenzeTracciatiIdOperazioniGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer id, Integer pagina, Integer risultatiPerPagina) {
		GpContext ctx = null;
		String transactionId = null;

		String methodName = "pendenzeTracciatiGET";
		try(ByteArrayOutputStream baos= new ByteArrayOutputStream();){
			this.log.info("Esecuzione " + methodName + " in corso...");

			this.logRequest(uriInfo, httpHeaders, methodName, baos);

			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();

			// Parametri - > DTO Input

			ListaOperazioniTracciatoDTO listaOperazioniTracciatoDTO = new ListaOperazioniTracciatoDTO(user);

			listaOperazioniTracciatoDTO.setPagina(pagina);
			listaOperazioniTracciatoDTO.setLimit(risultatiPerPagina);
			listaOperazioniTracciatoDTO.setIdTracciato((long) id);

			// INIT DAO

			TracciatiDAO tracciatiDAO = new TracciatiDAO(); 

			// CHIAMATA AL DAO

			ListaOperazioniTracciatoDTOResponse listaTracciatiDTOResponse = tracciatiDAO.listaOperazioniTracciatoPendenza(listaOperazioniTracciatoDTO);

			// CONVERT TO JSON DELLA RISPOSTA

			List<OperazionePendenza> results = new ArrayList<>();
			for(Operazione operazione: listaTracciatiDTOResponse.getResults()) {
				OperazionePendenza rsModel = TracciatiConverter.toOperazioneTracciatoPendenzaRsModel(operazione);
				results.add(rsModel);
			}

			ListaOperazioniPendenza response = new ListaOperazioniPendenza(results, this.getServicePath(uriInfo),
					listaTracciatiDTOResponse.getTotalResults(), pagina, risultatiPerPagina);

			this.logResponse(uriInfo, httpHeaders, methodName, response.toJSON(null,this.serializationConfig), 200);
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(Status.OK).entity(response.toJSON(null,this.serializationConfig)),transactionId).build();

		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
	}



	public Response pendenzeTracciatiIdStampeGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer id) {
		String methodName = "pendenzeTracciatiIdStampeGET";  
		GpContext ctx = null;
		String transactionId = null;

		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 

		try(ByteArrayOutputStream baos= new ByteArrayOutputStream();){

			this.logRequest(uriInfo, httpHeaders, methodName, baos);

			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();

			LeggiTracciatoDTO leggiTracciatoDTO = new LeggiTracciatoDTO(user);
			leggiTracciatoDTO.setId((long) id);

			TracciatiDAO tracciatiDAO = new TracciatiDAO();
			Tracciato tracciato = tracciatiDAO.leggiTracciato(leggiTracciatoDTO);

			if(tracciato.getStato().equals(STATO_ELABORAZIONE.ELABORAZIONE))
				throw new NonTrovataException("Stampe avvisi non disponibili per il tracciato: elaborazione ancora in corso");

			TracciatoPendenzeEsito rsModel = TracciatiConverter.toTracciatoPendenzeEsitoRsModel(tracciato);
			DettaglioTracciatoPendenzeEsito esito = rsModel.getEsito();
			List<EsitoOperazionePendenza> inserimenti = esito.getInserimenti();

			ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(baos1);

			this.popolaZip(user, inserimenti, zos);

			zos.flush();
			zos.close();

			String zipFileName = (tracciato.getFileNameRichiesta().contains(".") ? tracciato.getFileNameRichiesta().substring(0, tracciato.getFileNameRichiesta().lastIndexOf(".")) : tracciato.getFileNameRichiesta()) + ".zip";
			byte[] b = baos1.toByteArray();
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(Status.OK).type(MediaType.APPLICATION_OCTET_STREAM).entity(b).header("content-disposition", "attachment; filename=\""+zipFileName+"\""),transactionId).build();
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}

	}

	private void popolaZip(IAutorizzato user, List<EsitoOperazionePendenza> inserimenti, ZipOutputStream zos)
			throws ServiceException, PendenzaNonTrovataException, NotAuthorizedException, NotAuthenticatedException,
			IOException {
		boolean addError = true;
		if(inserimenti != null && !inserimenti.isEmpty()) {
			PendenzeDAO pendenzeDAO = new PendenzeDAO();
			for (EsitoOperazionePendenza esitoOperazionePendenza : inserimenti) {
				if(esitoOperazionePendenza.getStato().equals(StatoOperazionePendenza.ESEGUITO) && esitoOperazionePendenza.getEsito().equals("ADD_OK")) { 
					addError = false; // ho trovato almeno un avviso da stampare

					LeggiPendenzaDTO leggiPendenzaDTO = new LeggiPendenzaDTO(user);

					String idDominio = null;
					String numeroAvviso = null;
					try {
						Avviso avviso = (Avviso) esitoOperazionePendenza.getDati();
						idDominio = avviso.getIdDominio();
						numeroAvviso = avviso.getNumeroAvviso();
					} catch (Exception e) {
						java.util.LinkedHashMap<?,?> map = (LinkedHashMap<?, ?>) esitoOperazionePendenza.getDati();
						idDominio =(String)map.get("idDominio");
						numeroAvviso =(String)map.get("numeroAvviso");
					}

					leggiPendenzaDTO.setIdDominio(idDominio);
					leggiPendenzaDTO.setNumeroAvviso(numeroAvviso);
					LeggiPendenzaDTOResponse leggiPendenzaDTOResponse = pendenzeDAO.leggiAvvisoPagamento(leggiPendenzaDTO);

					String pdfFileName = idDominio + "_" + numeroAvviso + ".pdf"; 
					ZipEntry tracciatoOutputEntry = new ZipEntry(pdfFileName );
					zos.putNextEntry(tracciatoOutputEntry);
					zos.write(leggiPendenzaDTOResponse.getAvvisoPdf());
					zos.flush();
					zos.closeEntry();
				}
			}
		} 

		if(addError){
			ZipEntry tracciatoOutputEntry = new ZipEntry("errore.txt");
			zos.putNextEntry(tracciatoOutputEntry);
			zos.write("Attenzione: non sono presenti inserimenti andati a buon fine nel tracciato selezionato.".getBytes());
			zos.flush();
			zos.closeEntry();
		}
	}

	private String getBodyPartFileName (BodyPart bodyPart) throws Exception{
		String partName =  null;
		String[] headers = bodyPart.getHeader(it.govpay.rs.BaseController.PARAMETRO_CONTENT_DISPOSITION);
		if(headers != null && headers.length > 0){
			String header = headers[0];

			// in due parti perche il suffisso con solo " imbrogliava il controllo
			int prefixIndex = header.indexOf(it.govpay.rs.BaseController.PREFIX_FILENAME);
			if(prefixIndex > -1){
				partName = header.substring(prefixIndex + it.govpay.rs.BaseController.PREFIX_FILENAME.length());

				int suffixIndex = partName.indexOf(it.govpay.rs.BaseController.SUFFIX_FILENAME);
				partName = partName.substring(0,suffixIndex);
			}
		}

		return partName;
	}

}


