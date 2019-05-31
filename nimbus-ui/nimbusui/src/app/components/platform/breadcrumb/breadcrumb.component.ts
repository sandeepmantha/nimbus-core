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

import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { of as observableOf,  Observable, Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { BreadcrumbService } from './breadcrumb.service';
import { Breadcrumb } from './../../../model/breadcrumb.model';
/**
 * \@author Tony.Lopez
 * \@whatItDoes 
 * 
 * \@howToUse 
 * 
 */
@Component({
    selector: 'nm-breadcrumb',
    template: `
        <div class="breadcrumb-bar"> 
            <ol class="breadcrumb">
                <li *ngFor="let breadcrumb of breadcrumbs" class="breadcrumb-item">
                    <a [routerLink]="[breadcrumb.url]">{{breadcrumb.label}}</a>
                </li>
            </ol>
        </div>
    `
})
export class BreadcrumbComponent {

    public breadcrumbs: Breadcrumb[];
    subscribers: Subscription[] = [];
    firstTimeLoad = true;

    constructor (private _activatedRoute: ActivatedRoute, 
        private _router: Router,
        private _breadcrumbService: BreadcrumbService) {
        
        // initialize breadcrumbs as empty
        this.breadcrumbs = [];
        // retrieve labels
        this.breadcrumbs.forEach(b => {
                b.label = b.id;
        });

        //subscribe to the NavigationEnd event and load the breadcrumbs.
        this.subscribers.push(this._router.events.pipe(filter(event => event instanceof NavigationEnd))
                .subscribe((event) => {
            this._loadBreadcrumbs();
        }));
    }

    ngOnDestroy() {
        if (this.subscribers && this.subscribers.length > 0) {
            this.subscribers.forEach(s => s.unsubscribe());
        }
    }

    private _loadBreadcrumbs(): void {
        // if activatedRoute is the "home" route, show no breadcrumbs
        if (!this._breadcrumbService.isHomeRoute(this._activatedRoute)) {
            if(this.firstTimeLoad) {
                this.breadcrumbs = [];
                let crumb = this._breadcrumbService.getHomeBreadcrumb();
                if(crumb) {
                    this.firstTimeLoad = false;
                    this.breadcrumbs.push(crumb);
                }
            }
            let url = this._router.url.split('/');
            let cr = null;
            let ind = 0;
            //check if the page to be navigated is already in the session store for bread crumbs
            if(url.length > 3) {
                cr = this._breadcrumbService.getByPageId(url[3].split('?')[0]);
            }
            //check if the domain of the page to be navigated is part of the breadcrumb already
            if(this.breadcrumbs.length > 0) {
                ind = this.breadcrumbs.findIndex(b => b.url.split('/')[2] == url[2])
            }
            //check if the navigation is via the menu link so that the breadcrumb can be set appropriately
            if(cr && cr.url.indexOf('?') > 0) {
                let index = this.breadcrumbs.findIndex(d => d.url.split('/')[2] == cr.url.split('/')[3].split('=')[1]);
                if(index > -1)
                    this.breadcrumbs.splice((index + 1),this.breadcrumbs.length - (index+1));
            } else if(ind > -1) { //check if the navigation done via buttonclick/link etc has a domain already in breadcrumb
                this.breadcrumbs.splice((ind+1), this.breadcrumbs.length - (ind+1));
            }
            if(cr) {
                //add the crumb to the list
                this.breadcrumbs.push(this._breadcrumbService.addBreadCrumb(cr));
            }
        } else {

            // if it is the "home" route, reset the breadcrumbs
            this.breadcrumbs = [];
            this.firstTimeLoad = true;
        }
    }
}
