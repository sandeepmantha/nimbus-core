import { Param } from './../../../../shared/param-state';
'use strict';
import { TestBed, async } from '@angular/core/testing';
import { GrowlModule, AccordionModule, PickListModule, ListboxModule, CalendarModule, DataTableModule, DropdownModule, FileUploadModule, RadioButtonModule, CheckboxModule } from 'primeng/primeng';
import { FormsModule, ReactiveFormsModule, Validators, FormGroup, ValidatorFn, FormControl } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { EventEmitter, Input, Component } from '@angular/core';

import { CheckBoxGroup } from './checkbox-group.component';
import { TooltipComponent } from '../../../platform/tooltip/tooltip.component';
import { PageService } from '../../../../services/page.service';
import { WebContentSvc } from '../../../../services/content-management.service';
import { CustomHttpClient } from '../../../../services/httpclient.service';
import { LoaderService } from '../../../../services/loader.service';
import { ConfigService } from '../../../../services/config.service';
import { LoggerService } from '../../../../services/logger.service';
import { Subject } from 'rxjs';
// import { InputLegend } from '../../../platform/form/elements/input-legend.component';
import { configureTestSuite } from 'ng-bullet';
import { setup, TestContext } from '../../../../setup.spec';
import { ValidationUtils } from '../../validators/ValidationUtils';
import { fieldValueParam } from 'mockdata';
import { By } from '@angular/platform-browser';
import { ServiceConstants } from '../../../../services/service.constants';
import { WindowRefService } from '../../../../services/window-ref.service';
let pageService;

@Component({
  selector: 'nm-input-legend',
  template: `<div></div>
   `
})

export class InputLegend {
  @Input() required: boolean;
  @Input() element: any;
}

class MockPageService {
  public eventUpdate$: Subject<any>;
  public validationUpdate$: Subject<any>;

  constructor() {
    this.eventUpdate$ = new Subject();
    this.validationUpdate$ = new Subject();
  }

  logError(a) {
    this.eventUpdate$.next(a);
  }

  notifyErrorEvent(a) {
    this.validationUpdate$.next(a);
  }

  postOnChange(a, b, c) {

  }
}

const declarations = [
  CheckBoxGroup,
  TooltipComponent,
  InputLegend
];
const imports = [
GrowlModule,
AccordionModule, 
PickListModule, 
ListboxModule, 
CalendarModule, 
DataTableModule, 
DropdownModule, 
FileUploadModule, 
RadioButtonModule, 
CheckboxModule,
FormsModule, 
ReactiveFormsModule,
HttpModule,
HttpClientTestingModule
];
const providers = [
{provide: PageService, useClass: MockPageService},
WebContentSvc,
CustomHttpClient,
LoaderService,
ConfigService,
LoggerService,
WindowRefService
];
let fixture, hostComponent;
describe('CheckBoxGroup', () => {

  configureTestSuite(() => {
    setup( declarations, imports, providers);
  });


  beforeEach(() => {
    ServiceConstants.LOCALE_LANGUAGE = 'en-US';
    fixture = TestBed.createComponent(CheckBoxGroup);
    hostComponent = fixture.debugElement.componentInstance;
    // const fg = new FormGroup({});
    // const checks: ValidatorFn[] = [];
    // checks.push(Validators.required);
    // fg.addControl(checkBoxGroupElement.config.code, new FormControl(checkBoxGroupElement.leafState, checks));
    // hostComponent.form = fg;
    hostComponent.form = new FormGroup({
      firstName: new FormControl(),
      surveyLabel: new FormControl()
   });
    // hostComponent.element = fieldValueParam;
    hostComponent.element = checkBoxGroupElement as Param;
    pageService = TestBed.get(PageService);
  });

  it('should create the CheckBoxGroup',async(() => {
    expect(hostComponent).toBeTruthy();
  }));


  it('nm-input-legend should be created', async(() => {
    fixture.detectChanges();
    const debugElement = fixture.debugElement;
    const nmImputLegendEle = debugElement.query(By.css('nm-input-legend'));
    expect(nmImputLegendEle).toBeTruthy();
  }));

  it('p-checkbox should be created', async(() => {
    fixture.detectChanges();
    const debugElement = fixture.debugElement;
    const pCheckBoxEle = debugElement.query(By.css('p-checkbox'));
    expect(pCheckBoxEle).toBeTruthy();
  }));

  it('value property should be updated',async(() => {
    hostComponent.value = 'test';
    expect(hostComponent.value).toEqual('test');
  }));

  it('registerOnChange() should update the onChange property',async(() => {
    const test = () => {};
    hostComponent.registerOnChange(test);
    expect(hostComponent.onChange).toEqual(test);
  }));

  it('registerOnTouched() should update the onTouched property',async(() => {
    const test = () => {};
    hostComponent.registerOnTouched(test);
    expect(hostComponent.onTouched).toEqual(test);
  }));

  it('setState() should update call cd.markForCheck()',async(() => {
    const test = { element: { leafState: '' } };
    const spy = spyOn((hostComponent as any).cd, 'markForCheck').and.callThrough();
    hostComponent.setState('test', test);
    expect(spy).toHaveBeenCalled();
    expect(test.element.leafState).toEqual('test');
  }));

  it('emitValueChangedEvent() should call the controlValueChanged.emit()',async(() => {
    spyOn(hostComponent.controlValueChanged, 'emit').and.callThrough();
    hostComponent.form = null;
    hostComponent.emitValueChangedEvent({ element: '' }, '');
    expect(hostComponent.controlValueChanged.emit).toHaveBeenCalled();
  }));

  it('ngOnInit() should update the requiredCss property as false based on element.activeValidationGroups',async(() => {
    hostComponent.element.activeValidationGroups = ['1'];
    spyOn(ValidationUtils, 'buildStaticValidations').and.returnValue('');
    spyOn(ValidationUtils, 'rebindValidations').and.returnValue('');
    hostComponent.ngOnInit();
    expect(hostComponent.requiredCss).toBeFalsy();
  }));

  it('ngOnInit() should update the value',async(() => {
    hostComponent.element.leafState = [1];
    spyOn(ValidationUtils, 'buildStaticValidations').and.returnValue('');
    spyOn(ValidationUtils, 'rebindValidations').and.returnValue('');
    hostComponent.ngOnInit();
    expect(hostComponent.value).toEqual([1]);
  }));

  it('ngOnInit() should not subscribe to pageService.eventUpdate$',async(() => {
    spyOn(pageService.eventUpdate$, 'subscribe').and.callThrough();
    hostComponent.form.controls[hostComponent.element.config.code] = null;
    hostComponent.ngOnInit();
    expect(pageService.eventUpdate$.subscribe).not.toHaveBeenCalled();
  }));

  it('ngOnInit() should call form.controls[eve.config.code].setValue()',() => {
    fixture.whenStable().then(() => {
      const eve = { leafState: 'tLeaf', path: 'atest', config: { code: 'firstName' } };
      spyOn(ValidationUtils, 'buildStaticValidations').and.returnValue('');
      spyOn(ValidationUtils, 'rebindValidations').and.returnValue('');
      spyOn(ValidationUtils, 'applyelementStyle').and.returnValue('');
      spyOn(ValidationUtils, 'assessControlValidation').and.returnValue('');
      console.log('hostComponent.form.controls[eve.config.code]', hostComponent.form.controls['firstName']);
      const frmCtrl = hostComponent.form.controls[eve.config.code];
      hostComponent.element.path = 'atest';
      spyOn(frmCtrl, 'setValue').and.returnValue('');
      hostComponent.ngOnInit();
      pageService.logError(eve);
      expect(frmCtrl.setValue).toHaveBeenCalled();
    });
  });

  it('ngOnInit() should call form.controls[eve.config.code].reset()',() => {
    fixture.whenStable().then(() => {
      const eve = { leafState: null, path: 'atest', config: { code: 'firstName' } };
      const frmCtrl = hostComponent.form.controls[eve.config.code];
      hostComponent.element.path = 'atest';
      spyOn(frmCtrl, 'reset').and.returnValue('');
      hostComponent.ngOnInit();
      pageService.logError(eve);
      expect(frmCtrl.reset).toHaveBeenCalled();
    });
  });

  it('ngOnInit() should not call form.controls[eve.config.code].setValue() and reset() in form',() => {
    fixture.whenStable().then(() => {
      const eve = { leafState: null, path: '1atest', config: { code: 'firstName' } };
      const frmCtrl = hostComponent.form.controls[eve.config.code];
      hostComponent.element.path = 'atest';
      spyOn(frmCtrl, 'reset').and.returnValue('');
      spyOn(frmCtrl, 'setValue').and.returnValue('');
      hostComponent.ngOnInit();
      pageService.logError(eve);
      expect(frmCtrl.reset).not.toHaveBeenCalled();
      expect(frmCtrl.setValue).not.toHaveBeenCalled();
    });
  });

  it('ngOnInit() should update requiredCss property',async(() => {
    hostComponent.element.path = 'test';
    const eve = { activeValidationGroups: [1], leafState: 'tLeaf', path: 'test', config: { code: 'firstName' } };
    const frmCtrl = hostComponent.form.controls[eve.config.code];
    spyOn(ValidationUtils, 'rebindValidations').and.returnValue('');
    hostComponent.ngOnInit();
    pageService.notifyErrorEvent(eve);
    expect(hostComponent.requiredCss).toBeFalsy();
    expect(ValidationUtils.rebindValidations).toHaveBeenCalled();
  }));

  it('ngOnInit() should update requiredCss property on eve.activeValidationGroups.length is 0',() => {
    fixture.whenStable().then(() => {
      hostComponent.element.path = 'test';
      const eve = { activeValidationGroups: [], leafState: 'tLeaf', path: 'test', config: { code: 'firstName' } };
      const frmCtrl = hostComponent.form.controls[eve.config.code];
      spyOn(ValidationUtils, 'applyelementStyle').and.returnValue(true);
      hostComponent.ngOnInit();
      pageService.notifyErrorEvent(eve);
      expect(hostComponent.requiredCss).toBeTruthy();
      expect(ValidationUtils.applyelementStyle).toHaveBeenCalled();
    });
  });

  it('ngOnInit() should call pageService.postOnChange',async(() => {
    const eve = { config: { uiStyles: { attributes: { postEventOnChange: true } } } };
    spyOn(pageService, 'postOnChange').and.callThrough();
    spyOn(ValidationUtils, 'rebindValidations').and.returnValue('');
    hostComponent.ngOnInit();
    hostComponent.controlValueChanged.next(eve);
    expect(pageService.postOnChange).toHaveBeenCalled();
  }));

  it('ngOnInit() should not call pageService.postOnChange',async(() => {
    const eve = { config: { uiStyles: { attributes: { postEventOnChange: false } } } };
    spyOn(pageService, 'postOnChange').and.callThrough();
    spyOn(ValidationUtils, 'rebindValidations').and.returnValue('');
    hostComponent.ngOnInit();
    hostComponent.controlValueChanged.next(eve);
    expect(pageService.postOnChange).not.toHaveBeenCalled();
  }));

});


const checkBoxGroupElement: any = {
  "config": {
      "active": false,
      "required": false,
      "id": "910",
      "code": "surveyLabel",
      "validations": null,
      "uiNatures": [],
      "uiStyles": {
          "isLink": false,
          "isHidden": false,
          "name": "ViewConfig.CheckBoxGroup",
          "attributes": {
              "hidden": false,
              "readOnly": false,
              "submitButton": true,
              "showName": true,
              "pageSize": 25,
              "browserBack": false,
              "showAsLink": false,
              "help": "",
              "cssClass": "",
              "dataEntryField": true,
              "labelClass": "anthem-label",
              "level": "0",
              "alias": "CheckBoxGroup",
              "controlId": "",
              "postEventOnChange": false,
              "cols": ""
          }
      },
      "type": {
          "collection": false,
          "nested": false,
          "name": "string"
      }
  },
  "enabled": true,
  "visible": true,
  "activeValidationGroups": [],
  "collectionParams": [],
  "configId": "910",
  "path": "/petassessmentview/vpMain/vtMain/vsPetGeneralAssessment/petForm/petFormBody/petAssessment_Accordion_tab/surveyLabel",
  "type": {
      "nested": false,
      "name": "string",
      "collection": false
  },
  "message": [],
  "values": [
    {
        "code": "Routine vaccination appointment/checkup",
        "label": "Routine vaccination appointment/checkup"
    },
    {
        "code": "Medical problem/illness appointment",
        "label": "Medical problem/illness appointment"
    },
    {
        "code": "Emergency visit",
        "label": "Emergency visit"
    },
    {
        "code": "Grooming",
        "label": "Grooming"
    },
    {
        "code": "Boarding",
        "label": "Boarding"
    },
    {
        "code": "To purchase food or medications",
        "label": "To purchase food or medications"
    },
    {
        "code": "Other",
        "label": "Other"
    }
],
  "labels": [
      {
          "locale": "en-US",
          "text": "Please rate each part of your visit:",
          "helpText": "testing tooltip"
      }
  ],
  "elemLabels": {}
};