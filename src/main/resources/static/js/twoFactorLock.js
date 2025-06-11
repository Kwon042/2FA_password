document.addEventListener('DOMContentLoaded', () => {
  const lockTimeValue = document.getElementById('twoFactorLockTime')?.value;
  let lockTime = lockTimeValue ? new Date(lockTimeValue) : null;

  const lockMessageEl = document.getElementById('lock-message');
  const resetFormEl = document.getElementById('reset-form');
  const countdownEl = document.getElementById('countdown');
  const unlockMessageEl = document.getElementById('unlock-message'); // 잠금 해제 메시지

  if (lockTime) {
    const lockDurationSeconds = 60;
    const now = new Date();

    let diff = Math.floor((lockDurationSeconds * 1000 - (now - lockTime)) / 1000);

    if (diff > 0) {
      // 잠금 중일 때: 메시지 보이고 폼 숨기기
      lockMessageEl.style.display = 'block';
      resetFormEl.style.display = 'none';
      if (unlockMessageEl) unlockMessageEl.style.display = 'none';

      countdownEl.textContent = diff;

      const interval = setInterval(() => {
        diff--;
        if (diff <= 0) {
          clearInterval(interval);
          // 잠금 해제: 메시지 숨기고 폼 보이기
          lockMessageEl.style.display = 'none';
          resetFormEl.style.display = 'block';
          if (unlockMessageEl) {
            unlockMessageEl.style.display = 'block';

            // 10초 후 잠금 해제 메시지 숨김
            setTimeout(() => {
              unlockMessageEl.style.display = 'none';
            }, 10000);
          }
        } else {
          countdownEl.textContent = diff;
        }
      }, 1000);

      return; // 잠금 중이므로 아래 코드 실행 안함
    }
  }

  // 잠금 상태 없으면 폼 보이기, 메시지 숨기기
  if (lockMessageEl) lockMessageEl.style.display = 'none';
  if (resetFormEl) resetFormEl.style.display = 'block';
  if (unlockMessageEl) unlockMessageEl.style.display = 'none';

  // 기존 2FA 코드 입력 유효성 검사 부분 추가 (원하시면)
  const codeInput = document.getElementById('code');
  const feedback = document.getElementById('code-feedback');
  const submitBtn = document.getElementById('submitBtn');

  if(codeInput && feedback && submitBtn) {
      codeInput.addEventListener('input', () => {
          const val = codeInput.value.trim();

          // 6자리 숫자 정규식 체크
          const isValid = /^\d{6}$/.test(val);

          if (!isValid) {
              feedback.style.display = 'block';
              submitBtn.disabled = true;
          } else {
              feedback.style.display = 'none';
              submitBtn.disabled = false;
          }
      });
  }
});
