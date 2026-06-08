import { Component } from '@angular/core';
import { Navbar } from '../../layout/navbar/navbar';

/**
 * Component responsible for displaying webinar-related functionality.
 */
@Component({
  selector: 'app-webinars',
  imports: [Navbar],
  templateUrl: './webinars.html',
  styleUrl: './webinars.css',
})
export class Webinars {}