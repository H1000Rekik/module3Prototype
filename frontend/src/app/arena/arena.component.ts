import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { SubmissionService, Submission } from '../services/submission.service';

@Component({
  selector: 'app-arena',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './arena.component.html',
  styleUrls: ['./arena.component.scss']
})
export class ArenaComponent implements OnInit, OnDestroy {
  username = 'player1';
  code = `def solve(nums: list, target: int) -> list:
    # Write your solution here
    seen = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return []`;

  isRunning = false;
  currentStatus: 'idle' | 'pending' | 'processing' | 'done' = 'idle';
  result: Submission | null = null;
  history: Submission[] = [];
  activeTab: 'problem' | 'result' | 'history' = 'problem';

  private pollSub?: Subscription;

  constructor(private submissionService: SubmissionService) {}

  ngOnInit() {
    this.loadHistory();
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

  run() {
    this.execute('run');
  }

  submitCode() {
    this.execute('submit');
  }

  private execute(mode: 'run' | 'submit') {
    if (this.isRunning) return;
    this.isRunning = true;
    this.currentStatus = 'pending';
    this.result = null;
    this.activeTab = 'result';

    this.pollSub?.unsubscribe();

    this.submissionService.submit({
      username: this.username,
      code: this.code,
      mode
    }).subscribe({
      next: (res) => {
        this.currentStatus = 'processing';
        this.pollSub = this.submissionService.pollUntilDone(res.id).subscribe({
          next: (submission) => {
            this.result = submission;
            if (submission.status === 'DONE') {
              this.currentStatus = 'done';
              this.isRunning = false;
              this.loadHistory();
            }
          },
          error: () => {
            this.isRunning = false;
            this.currentStatus = 'idle';
          }
        });
      },
      error: () => {
        this.isRunning = false;
        this.currentStatus = 'idle';
      }
    });
  }

  loadHistory() {
    this.submissionService.getUserHistory(this.username).subscribe(h => {
      this.history = h;
    });
  }

  getVerdictClass(verdict: string): string {
    if (!verdict) return '';
    if (verdict === 'Accepted') return 'verdict-accepted';
    if (verdict === 'Compilation Error') return 'verdict-ce';
    return 'verdict-failed';
  }

  formatTime(ms: number): string {
    return ms ? `${ms}ms` : '—';
  }

  formatMemory(kb: number): string {
    return kb ? `${(kb / 1024).toFixed(1)} MB` : '—';
  }
}
