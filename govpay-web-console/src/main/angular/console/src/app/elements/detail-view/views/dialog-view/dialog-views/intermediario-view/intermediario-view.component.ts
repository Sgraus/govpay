import { AfterViewInit, Component, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { UtilService } from '../../../../../../services/util.service';
import { IFormComponent } from '../../../../../../classes/interfaces/IFormComponent';


@Component({
  selector: 'link-intermediario-view',
  templateUrl: './intermediario-view.component.html',
  styleUrls: ['./intermediario-view.component.scss']
})
export class IntermediarioViewComponent  implements IFormComponent, OnInit, AfterViewInit {

  @Input() fGroup: FormGroup;
  @Input() json: any;

  protected BASIC = UtilService.TIPI_AUTENTICAZIONE.basic;
  protected SSL = UtilService.TIPI_AUTENTICAZIONE.ssl;
  protected CLIENT = UtilService.TIPI_SSL.client;
  protected SERVER = UtilService.TIPI_SSL.server;

  // protected versioni: any[] = UtilService.TIPI_VERSIONE_API;
  protected _isBasicAuth: boolean = false;
  protected _isSslAuth: boolean = false;

  constructor() { }

  ngOnInit() {
    this.fGroup.addControl('denominazione_ctrl', new FormControl('', Validators.required));
    this.fGroup.addControl('idIntermediario_ctrl', new FormControl('', Validators.required));
    this.fGroup.addControl('principalPagoPa_ctrl', new FormControl(''));
    this.fGroup.addControl('abilita_ctrl', new FormControl());
    this.fGroup.addControl('url_ctrl', new FormControl('', Validators.required));
    // this.fGroup.addControl('versioneApi_ctrl', new FormControl('', Validators.required));
    this.fGroup.addControl('auth_ctrl', new FormControl(''));
  }

  ngAfterViewInit() {
    setTimeout(() => {
      if(this.json) {
        this.fGroup.controls['idIntermediario_ctrl'].disable();
        this.fGroup.controls['idIntermediario_ctrl'].setValue(this.json.idIntermediario);
        this.fGroup.controls['denominazione_ctrl'].setValue(this.json.denominazione);
        this.fGroup.controls['principalPagoPa_ctrl'].setValue(this.json.principalPagoPa);
        this.fGroup.controls['abilita_ctrl'].setValue(this.json.abilitato);
        this.fGroup.controls['auth_ctrl'].setValue('');
        if(this.json.servizioPagoPa) {
          this.fGroup.controls['url_ctrl'].setValue(this.json.servizioPagoPa.url);
          // this.fGroup.controls['versioneApi_ctrl'].setValue(this.json.servizioPagoPa.versioneApi);
          if(this.json.servizioPagoPa.auth) {
            let _sppaa = this.json.servizioPagoPa.auth;
            if(_sppaa.hasOwnProperty('username')) {
              this.addBasicControls();
              this.fGroup.controls['auth_ctrl'].setValue(this.BASIC);
              this.fGroup.controls['username_ctrl'].setValue((_sppaa.username)?_sppaa.username:'');
              this.fGroup.controls['password_ctrl'].setValue((_sppaa.password)?_sppaa.password:'');
              this._isBasicAuth = true;
            }
            if(_sppaa.hasOwnProperty('tipo')) {
              this.fGroup.controls['auth_ctrl'].setValue(this.SSL);
              this.addSslControls();
              this.fGroup.controls['ssl_ctrl'].setValue((_sppaa.tipo)?_sppaa.tipo:'');
              this.fGroup.controls['ksLocation_ctrl'].setValue((_sppaa.ksLocation)?_sppaa.ksLocation:'');
              this.fGroup.controls['ksPassword_ctrl'].setValue((_sppaa.ksPassword)?_sppaa.ksPassword:'');
              this.fGroup.controls['tsLocation_ctrl'].setValue((_sppaa.tsLocation)?_sppaa.tsLocation:'');
              this.fGroup.controls['tsPassword_ctrl'].setValue((_sppaa.tsPassword)?_sppaa.tsPassword:'');
              this._isSslAuth = true;
            }
          }
        }
      }
    });
  }

  protected _onAuthChange(target) {
    this._isBasicAuth = false;
    this._isSslAuth = false;
    this.removeBasicControls();
    this.removeSslControls();
    switch(target.value) {
      case this.BASIC:
        this.addBasicControls();
        this._isBasicAuth = true;
        break;
      case this.SSL:
        this.addSslControls();
        this._isSslAuth = true;
        break;
    }
  }

  protected addBasicControls() {
    this.fGroup.addControl('username_ctrl', new FormControl('', Validators.required));
    this.fGroup.addControl('password_ctrl', new FormControl('', Validators.required));
  }

  protected addSslControls() {
    this.fGroup.addControl('ssl_ctrl', new FormControl('', Validators.required));
    this.fGroup.addControl('ksLocation_ctrl', new FormControl('', Validators.required));
    this.fGroup.addControl('ksPassword_ctrl', new FormControl('', Validators.required));
    this.fGroup.addControl('tsLocation_ctrl', new FormControl(''));
    this.fGroup.addControl('tsPassword_ctrl', new FormControl(''));
  }

  protected removeBasicControls() {
    this.fGroup.removeControl('username_ctrl');
    this.fGroup.removeControl('password_ctrl');
  }

  protected removeSslControls() {
    this.fGroup.removeControl('ssl_ctrl');
    this.fGroup.removeControl('ksLocation_ctrl');
    this.fGroup.removeControl('ksPassword_ctrl');
    this.fGroup.removeControl('tsLocation_ctrl');
    this.fGroup.removeControl('tsPassword_ctrl');
  }

  /**
   * Interface IFormComponent: Form controls to json object
   * @returns {any}
   */
  mapToJson(): any {
    let _info = this.fGroup.value;
    let _json:any = {};
    _json.idIntermediario = (!this.fGroup.controls['idIntermediario_ctrl'].disabled)?_info['idIntermediario_ctrl']:this.json.idIntermediario;
    _json.abilitato = _info['abilita_ctrl'] || false;
    _json.denominazione = (_info['denominazione_ctrl'])?_info['denominazione_ctrl']:null;
    _json.principalPagoPa = (_info['principalPagoPa_ctrl'])?_info['principalPagoPa_ctrl']:null;
    _json.servizioPagoPa = {
      auth: null,
      url: _info['url_ctrl'],
      // versioneApi: _info['versioneApi_ctrl']
    };
    if(_info.hasOwnProperty('username_ctrl')) {
      _json.servizioPagoPa['auth'] = {
        username: _info['username_ctrl'],
        password: _info['password_ctrl']
      };
    }
    if(_info.hasOwnProperty('ssl_ctrl')) {
      _json.servizioPagoPa['auth'] = {
        tipo: _info['ssl_ctrl'],
        ksLocation: _info['ksLocation_ctrl'],
        ksPassword: _info['ksPassword_ctrl'],
        tsLocation: '',
        tsPassword: ''
      };
      if(_info.hasOwnProperty('tsLocation_ctrl')) {
        _json.servizioPagoPa['auth'].tsLocation = _info['tsLocation_ctrl'];
      }
      if(_info.hasOwnProperty('tsPassword_ctrl')) {
        _json.servizioPagoPa['auth'].tsPassword = _info['tsPassword_ctrl'];
      }
    }
    if(_json.servizioPagoPa.auth == null) { delete _json.servizioPagoPa.auth; }

    return _json;
  }
}
