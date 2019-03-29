/**
 * @license
 * Copyright 2016-2018 the original author or authors.
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
	Component, Input, ViewChild, ViewEncapsulation,
	ElementRef, ViewChildren, QueryList, forwardRef, ChangeDetectorRef
} from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';
import { FormGroup, FormControl, FormArray, FormBuilder, Validators } from '@angular/forms';
import { FormArrayName } from '@angular/forms/src/directives/reactive_directives/form_group_name';
import { Param } from '../../../shared/param-state';
import { FileService } from './../../../services/file.service';
import { BaseElement } from '../base-element.component';
import { WebContentSvc } from './../../../services/content-management.service';
import { LoggerService } from './../../../services/logger.service';
import { Message } from '../../../shared/message';
import { ServiceConstants } from './../../../services/service.constants';
import { FileUpload } from 'primeng/primeng';

export const CUSTOM_INPUT_CONTROL_VALUE_ACCESSOR: any = {
	provide: NG_VALUE_ACCESSOR,
	useExisting: forwardRef(() => FileUploadComponent),
	multi: true
};

/**
 * \@author Dinakar.Meda
 * \@author Vivek.Kamineni
 * \@whatItDoes 
 * 
 * \@howToUse 
 * 
 */
@Component({
	selector: 'nm-upload',
	providers: [CUSTOM_INPUT_CONTROL_VALUE_ACCESSOR, WebContentSvc ],
	styles: [
	],
	encapsulation: ViewEncapsulation.None,
	template: `
			<p-fileUpload #pfu name="pfileupload" 
				[showUploadButton]="true" 
				[showCancelButton]="false"
				[customUpload]="true"
				[multiple]="element?.config?.uiStyles?.attributes?.multiple"
				(onSelect)="addFiles($event)"
				(onRemove)="removeFiles($event)"
				[disabled]="disabled"
				(uploadHandler)="customUpload($event)"
				url="{{element?.config?.uiStyles?.attributes?.url}}" 
				accept="{{element?.config?.uiStyles?.attributes?.type}}">
				<ng-template pTemplate="toolbar" [ngIf]="element?.config?.uiStyles?.attributes?.behavior == 'CONVERSION'">
					<p-dropdown 
						[options]="element.values | selectItemPipe" 
						[disabled]="!element.enabled"
						(onChange)="evalUpload()"
						[(ngModel)] = "dropdownValue"
						class="form-control">
					</p-dropdown>
       			</ng-template>
				<ng-template pTemplate="content"  [ngIf]="element?.config?.uiStyles?.attributes?.behavior == 'CONVERSION'">
					<div>Custom UI to manage uploaded files</div>
				</ng-template>
				<ng-template let-file pTemplate="file" [ngIf]="element?.config?.uiStyles?.attributes?.behavior == 'CONVERSION'">
						<div class="ui-fileupload-row">
							<div>{{file.name}}</div>
							<div>{{file.domain}}</div>
							<div>
								<button type="button" icon="pi pi-times" pButton (click)="pfu.remove($event,hasFile(file))" [disabled]="file.upload"></button>
							</div>
						</div>
				</ng-template>
			</p-fileUpload>
	`
})

export class FileUploadComponent extends BaseElement implements ControlValueAccessor {

	@Input() element: Param;
	@Input() form: FormGroup;
	@Input('value') _value: File[];
	dropdownValue: any;
	selectedFiles: File[];
	@ViewChild('pfu') pfu: FileUpload;	
	disabled: boolean;

	constructor(private fileService: FileService, private _wcs: WebContentSvc, private logger: LoggerService,  protected cd: ChangeDetectorRef) {
		super(_wcs);
	}

	public onChange: any = (_) => { /*Empty*/ }
	public onTouched: any = () => { /*Empty*/ }

	get value() {
		return this._value;
	}

	set value(val) {
		this._value = val;
		this.onChange(val);
		this.onTouched();
	}

	writeValue(value) {
		if (value) {
		}
	}

	registerOnChange(fn) {
		this.onChange = fn;
	}

	registerOnTouched(fn) {
		this.onTouched = fn;
	}

	ngOnInit() {
		this.evalUpload();
		this.selectedFiles = [];
		this.fileService.metaData = this.element.config.uiStyles.attributes.metaData;
	
		this.fileService.errorEmitter$.subscribe(data => {

			this.pfu.files = [];
			
			this.element.message = [];
			this.element.message.push(Message.createMessage("DANGER", "TOAST", "File Upload Failed", 10000, ''));
			
        });
	}

	addFiles(event) {
		let files = event.files;

		for (var p = 0; p < files.length; p++) {
			if (this.hasFile(files[p]) == -1) {

				if (!this.element.config.uiStyles.attributes.multiple) {
					this.selectedFiles = [];
					this.value = this.selectedFiles;
				}
				if (this.pfu.validate(files[p])) {
					let file = files[p];
					file['domain'] = this.dropdownValue;	
					file['upload'] = false;
					file['postUrl'] = this.element.config.uiStyles.attributes.url? this.element.config.uiStyles.attributes.url : ServiceConstants.FILE_IMPORT + '?domain='+this.dropdownValue;				
					this.selectedFiles.push(file);
					this.value = this.selectedFiles;
				}
			}

		}
		this.dropdownValue = null;
		this.evalUpload();
	}

	removeFiles(event) {
		let index = this.hasFile(event.file);
		if (index > -1) {
			this.selectedFiles.splice(index, 1);
		}
		this.value = this.selectedFiles;

	}

	hasFile(file: File): number {
		let index = -1;
		for (var p=0; p<this.selectedFiles.length; p++) {
			let selFile = this.selectedFiles[p];
			if (selFile.name == file.name && selFile.size == file.size) {
				index = p;
			}
		}
		return index;
	}

	evalUpload() {
		if(this.element.config.uiStyles.attributes.behavior == 'CONVERSION' && this.element.enabled) {
			if(this.dropdownValue) {
				this.disabled = false;
			} else {
				this.disabled = true;
			}
		} else {
			this.disabled = !this.element.enabled;
		}
		this.cd.detectChanges();
			
	}
	customUpload(event) {
		let files = this.value;
		if(files) {
			for(let p=0; p<files.length; p++){
				this.fileService.uploadFile(files[p], this.form)
					.subscribe(data => {
						files[p]['upload'] = true;	
					},
					error => this.logger.error(error),
					() => this.logger.info('File uploaded ..')
				);
			}
		}
	}
}
