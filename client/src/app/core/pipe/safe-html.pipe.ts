import { Pipe, PipeTransform } from '@angular/core';
import {DomSanitizer} from '@angular/platform-browser';

@Pipe({
  name: 'safeHtml'
})
export class SafeHtmlPipe implements PipeTransform {

  constructor(private sanitized: DomSanitizer) {}

  transform(value: unknown, ...args: unknown[]): unknown {
    return this.sanitized.bypassSecurityTrustHtml(<string>value);
  }

}
