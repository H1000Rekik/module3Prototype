import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-lobby',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.css']
})
export class LobbyComponent implements OnInit {
  username = '';
  joinRoomId = '';

  constructor(private http: HttpClient, private router: Router) { }

  ngOnInit() {
    this.username = sessionStorage.getItem('username') || '';
    if (!this.username) {
      this.router.navigate(['/auth']);
    }
  }

  createRoom() {
    this.http.post('http://localhost:8087/api/match/create', { username: this.username })
      .subscribe({
        next: (room: any) => {
          this.router.navigate(['/match', room.roomId]);
        },
        error: err => console.error(err)
      });
  }

  joinRoom() {
    if (this.joinRoomId) {
      this.router.navigate(['/match', this.joinRoomId]);
    }
  }
}
