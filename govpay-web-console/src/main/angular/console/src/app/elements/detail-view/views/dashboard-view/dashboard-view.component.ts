import { Component, OnInit } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { UtilService } from '../../../../services/util.service';
import { LinkService } from '../../../../services/link.service';

import * as moment from 'moment';

@Component({
  selector: 'link-dashboard-view',
  templateUrl: './dashboard-view.component.html',
  styleUrls: ['./dashboard-view.component.scss']
})
export class DashboardViewComponent implements OnInit {

  news: any[] = [];
  _isLoading:boolean = false;

  _PICService: string = '';
  _PICExamService: string = '';
  _PICDiffService: string = '';
  _PFService: string = '';
  _PFExamService: string = '';
  _PFDiffService: string = '';

  protected _LinkBasePath: string = UtilService.LINK_BASE_PATH();
  protected _dashboardConfig: any = UtilService.DASHBOARD_CARDS_CONFIG;
  protected DASHBOARD: string = UtilService.URL_DASHBOARD;

  constructor(private sanitizer: DomSanitizer, private ls: LinkService) {}

  ngOnInit() {
    this.initBadges();
    if(this._dashboardConfig.news) {
      const url = 'https://api.github.com/repos/link-it/GovPay/releases';
      const xhr = new XMLHttpRequest();
      xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
          if (xhr.status === 200) {
            this._isLoading = false;
            this.news = xhr.response?JSON.parse(xhr.response):[];
            this.news.forEach((_news) => {
              _news.body_html = this._trustHtml(_news.body_html)
            });
          } else {
            this._isLoading = false;
            console.log('News Error: ' + xhr.status);
          }
        }
      }.bind(this);
      this._isLoading = true;
      xhr.open('GET', url);
      xhr.timeout = UtilService.TIMEOUT;
      xhr.setRequestHeader('Accept', 'application/vnd.github.v3.html+json');
      xhr.send();
    }
  }

  protected _trustHtml(_html) {
    return this.sanitizer.bypassSecurityTrustHtml(_html);
  }

  /**
   * Navigazione via badge-card
   * @param {number} value
   * @private
   */
  protected _route(value: number) {
    UtilService.DASHBOARD_LINKS_PARAMS = { method: '', params: [] };
    UtilService.DASHBOARD_LINKS_PARAMS.method = UtilService.URL_PAGAMENTI;
    switch(value) {
      case 0:
        //Rifiutati
        UtilService.SaveCookie(UtilService.COOKIE_RIFIUTATI);
        UtilService.DASHBOARD_LINKS_PARAMS.params.push({ controller: 'stato', value: 'FALLITO' });
        UtilService.DASHBOARD_LINKS_PARAMS.params.push({ controller: 'verificato', value: false });
        break;
      case 1:
        //Sospesi
        UtilService.SaveCookie(UtilService.COOKIE_SOSPESI);
        // UtilService.BACK_IN_TIME_DATE = moment().subtract(UtilService.BACK_IN_TIME(), 'h').format('YYYY-MM-DDTHH:mm:ss');
        UtilService.DASHBOARD_LINKS_PARAMS.params.push({ controller: 'stato', value: 'IN_CORSO' });
        UtilService.DASHBOARD_LINKS_PARAMS.params.push({ controller: 'dataA', value: UtilService.BACK_IN_TIME_DATE });
        UtilService.DASHBOARD_LINKS_PARAMS.params.push({ controller: 'verificato', value: false });
        break;
    }
    this.ls.resetRouteReuseStrategy();
    this.ls.navigateTo([UtilService.DASHBOARD_LINKS_PARAMS.method]);
  }

  initBadges() {
    //Sospesi
    this._PICService = '';
    this._PICExamService = '';
    this._PICDiffService = '';

    UtilService.BACK_IN_TIME_DATE = moment().subtract(UtilService.BACK_IN_TIME(), 'h').format('YYYY-MM-DDTHH:mm:ss');
    this._PICService = UtilService.URL_PAGAMENTI+'?risultatiPerPagina=1&stato=IN_CORSO&verificato=false&dataA='+UtilService.BACK_IN_TIME_DATE;
    this._PICExamService = UtilService.URL_PAGAMENTI+'?risultatiPerPagina=1&stato=IN_CORSO&verificato=true&dataA='+UtilService.BACK_IN_TIME_DATE;

    this._PICDiffService = this._PICService + '&dataDa=';
    UtilService.COOKIE_SESSION = UtilService.ReadCookie(UtilService.COOKIE_SOSPESI);
    if(UtilService.COOKIE_SESSION) {
      this._PICDiffService += UtilService.COOKIE_SESSION;
    } else {
      this._PICDiffService += moment().format('YYYY-MM-DDTHH:mm:ss');
    }
    //Rifiutati
    this._PFService = '';
    this._PFExamService = '';
    this._PFDiffService = '';

    this._PFService = UtilService.URL_PAGAMENTI+'?risultatiPerPagina=1&stato=FALLITO&verificato=false';
    this._PFExamService = UtilService.URL_PAGAMENTI+'?risultatiPerPagina=1&stato=FALLITO&verificato=true';

    this._PFDiffService = this._PFService + '&dataDa=';
    UtilService.COOKIE_SESSION = UtilService.ReadCookie(UtilService.COOKIE_RIFIUTATI);
    if(UtilService.COOKIE_SESSION) {
      this._PFDiffService += UtilService.COOKIE_SESSION;
    } else {
      this._PFDiffService += moment().format('YYYY-MM-DDTHH:mm:ss');
    }
  }

}
