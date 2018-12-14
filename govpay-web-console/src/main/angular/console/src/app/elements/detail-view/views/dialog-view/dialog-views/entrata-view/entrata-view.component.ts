import { AfterViewInit, Component, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { UtilService } from '../../../../../../services/util.service';
import { IFormComponent } from '../../../../../../classes/interfaces/IFormComponent';
import { FormInput } from '../../../../../../classes/view/form-input';

@Component({
  selector: 'link-entrata-view',
  templateUrl: './entrata-view.component.html',
  styleUrls: ['./entrata-view.component.scss']
})
export class EntrataViewComponent implements IFormComponent,  OnInit, AfterViewInit {

  @Input() fGroup: FormGroup;
  @Input() json: any;

  protected contabilita_items: any[];

  protected _jsonFormInput: boolean = false;

  constructor(public us:UtilService) {
    this.contabilita_items = us.tipiContabilita();
  }

  ngOnInit() {
    if(this.json instanceof FormInput) {
      this._jsonFormInput = true;
      this.fGroup.addControl('idEntrata_ctrl', new FormControl('', Validators.required));
    } else {
      this.fGroup.addControl('idEntrata_ctrl', new FormControl('', Validators.required));
      this.fGroup.addControl('descrizione_ctrl', new FormControl('', Validators.required));
      this.fGroup.addControl('tipoContabilita_ctrl', new FormControl(''));
      this.fGroup.addControl('codiceContabilita_ctrl', new FormControl('', Validators.required));
      this.fGroup.addControl('codificaIUV_ctrl', new FormControl(''));
      this.fGroup.addControl('attivazione_ctrl', new FormControl(false));
      this.fGroup.addControl('pagaTerzi_ctrl', new FormControl(false));
    }
  }

  ngAfterViewInit() {
    setTimeout(() => {
      if(this.json) {
        if(this.json instanceof FormInput && this.json.values.length == 0) {
          this.fGroup.controls[ 'idEntrata_ctrl' ].disable();
        }
        if(!(this.json instanceof FormInput)) {
          this.fGroup.controls['idEntrata_ctrl'].disable();
          this.fGroup.controls['idEntrata_ctrl'].setValue((this.json.idEntrata)?this.json.idEntrata:'');
          this.fGroup.controls['descrizione_ctrl'].setValue((this.json.descrizione)?this.json.descrizione:'');
          this.fGroup.controls['tipoContabilita_ctrl'].setValue((this.json.tipoContabilita)?this.json.tipoContabilita:'');
          this.fGroup.controls['codiceContabilita_ctrl'].setValue((this.json.codiceContabilita)?this.json.codiceContabilita:'');
          this.fGroup.controls['codificaIUV_ctrl'].setValue((this.json.codificaIUV)?this.json.codificaIUV:'');
          this.fGroup.controls['attivazione_ctrl'].setValue(this.json.online);
          this.fGroup.controls['pagaTerzi_ctrl'].setValue(this.json.pagaTerzi);
        }
      }
    });
  }

  /**
   * Interface IFormComponent: Form controls to json object
   * @returns {any}
   */
  mapToJson(): any {
    let _info = this.fGroup.value;
    let _json:any = {};
    if(this.json instanceof FormInput) {
      _json.values = [];
      _info['idEntrata_ctrl'].forEach((el) => {
        _json.values.push({
          idEntrata: el.value,
          descrizione: el.label
        });
      });
    } else {
      _json.idEntrata = (!this.fGroup.controls['idEntrata_ctrl'].disabled)?_info['idEntrata_ctrl']:this.json.idEntrata;
      _json.descrizione = (_info['descrizione_ctrl'])?_info['descrizione_ctrl']:null;
      _json.tipoContabilita = (_info['tipoContabilita_ctrl'])?_info['tipoContabilita_ctrl']:null;
      _json.codiceContabilita = (_info['codiceContabilita_ctrl'])?_info['codiceContabilita_ctrl']:null;
      _json.codificaIUV = (_info['codificaIUV_ctrl'])?_info['codificaIUV_ctrl']:null;
      _json.online = _info['attivazione_ctrl'];
      _json.pagaTerzi = _info['pagaTerzi_ctrl'];
    }
    return _json;
  }

}
