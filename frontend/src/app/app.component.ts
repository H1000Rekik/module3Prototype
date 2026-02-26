import { Component } from '@angular/core';
import { ArenaComponent } from './arena/arena.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ArenaComponent],
  template: `<app-arena></app-arena>`,
  styles: [`
    :host { display: block; margin: 0; padding: 0; }
  `]
})
export class AppComponent {}
