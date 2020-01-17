/**
 * @license
 * Copyright 2016-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

import {
  ChangeDetectorRef,
  Component,
  forwardRef,
  ElementRef,
  ViewChild
} from '@angular/core';
import { NgModel, NG_VALUE_ACCESSOR } from '@angular/forms';
import { ControlSubscribers } from './../../../../services/control-subscribers.service';
import { CounterMessageService } from './../../../../services/counter-message.service';
import { BaseControl } from './base-control.component';
import { InputEvent } from './../../../../shared/param-config';
import { Event } from './../../../../shared/param-annotations.enum';

export const CUSTOM_INPUT_CONTROL_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => InputNumber),
  multi: true
};

/**
 * \@author Dinakar.Meda
 * \@author Sandeep.Mantha
 * \@whatItDoes
 *
 * \@howToUse
 *
 */
@Component({
  selector: 'nm-input-number',
  providers: [CUSTOM_INPUT_CONTROL_VALUE_ACCESSOR, ControlSubscribers],
  template: `
    <nm-input-label
      *ngIf="!isLabelEmpty && hidden != true && !this.hideLabel"
      [element]="element"
      [for]="element.config?.code"
      [required]="requiredCss"
    >
    </nm-input-label>
    {{value}}{{element.leafState}}
    <input
      *ngIf="hidden != true && readOnly == false"
      (click)="showMask=!showMask"
      (ngModelChange)="value = $event" 
      (blur)="showMask = true"
      [ngModel]="value | mask:showMask:element"
      [autocomplete]="element?.config?.uiStyles?.attributes?.autofill ? 'on' : undefined"
      [id]="element.config?.code"
      (focusout)="emitValueChangedEvent(this,value)"
      (input)="bindInputEvent ? onInput(): false"
      [type]="type"
      [disabled]="disabled"
      class="form-control text-input"
    />
   
    <input
      *ngIf="hidden == true"
      [id]="element.config?.code"
      type="hidden"
      [value]="element.leafState"
    />

    <pre class="print-only" *ngIf="hidden != true && readOnly == false">{{
      this.value
    }}</pre>

    <p style="margin-bottom:0rem;" *ngIf="readOnly == true">
      {{ element.leafState }}
    </p>
  `
})
export class InputNumber extends BaseControl<Number> {
  @ViewChild(NgModel) model: NgModel;
  showMask = true;
  bindInputEvent: boolean = false;
  inpEvt: InputEvent;

  constructor(
    controlService: ControlSubscribers,
    cd: ChangeDetectorRef,
    counterMessageService: CounterMessageService
  ) {
    super(controlService, cd, counterMessageService);
  }

  ngOnInit() {
    super.ngOnInit();
    this.element.config.uiStyles.attributes.inputEvent.forEach(evt => {
      if(evt.eventType == Event._input.toString()) {
        this.bindInputEvent = true;
        this.inpEvt = evt;
      }
    });
  }

  onInput() {
    if(this.inpEvt.charCountToPostOnce !=0 && this.inpEvt.charCountToPostOnce == this.value.toString().length) {
      this.emitValueChangedEvent(this,this.value);
    } else if(this.inpEvt.count != 0 && this.value.toString().length%this.inpEvt.count == 0){
      this.emitValueChangedEvent(this,this.value);
    }
  }
}
