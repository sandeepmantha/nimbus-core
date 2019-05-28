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

import { Input } from '@angular/core';
import { Param } from './../../shared/param-state';
import { ParamUtils } from './../../shared/param-utils';
import { PageService } from '../../services/page.service';
import { Subscription } from 'rxjs';

/**
 * \@author Tony Lopez
 * \@whatItDoes 
 * 
 * \@howToUse 
 * 
 */
export abstract class BaseLabel {

    @Input() element: Param;
    @Input() labelClass: Param;

    private subscription: Subscription;
    
    helpText: string;
    label: string;
    cssClass: string;

    constructor(protected pageService: PageService) {

    }

    ngOnInit() {
        this.load();
        this.subscription = this.pageService.eventUpdate$.subscribe(event => {
            if(event.path == this.element.path) {
                this.load();
            }
        });
    }

    ngOnDestroy() {
        if (this.subscription) {
            this.subscription.unsubscribe();
        }
    }

    load() {
        this.helpText = ParamUtils.getHelpText(this.element);
        this.label = ParamUtils.getLabelText(this.element);
        this.cssClass = this.loadCssClass();
    }

    /**
     * Get the css classes to apply for this element.
     */
    public loadCssClass(): string {
        let cssClass = ParamUtils.getLabelCss(this.element);
        if (this.labelClass) {
            cssClass += ' ' + this.labelClass;
        }
        return cssClass ? cssClass : '';
    }
}

