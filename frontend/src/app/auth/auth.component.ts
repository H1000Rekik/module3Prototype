import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent {
  username = '';
  password = '';

  constructor(private http: HttpClient, private router: Router) { }

  login() {
    if (!this.username) return;
    this.http.post('http://localhost:8087/api/auth/login', { username: this.username, password: this.password })
      .subscribe({
        next: (response: any) => {
          sessionStorage.setItem('username', this.username);
          sessionStorage.setItem('token', response.token);
          this.router.navigate(['/lobby']);
        },
        error: (err) => {
          console.error('Login failed', err);
          alert('Login failed. Try again.');
        }
      });
  }
}
