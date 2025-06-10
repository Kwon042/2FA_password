const lockTimeValue = document.getElementById('twoFactorLockTime').value;
let lockTime = lockTimeValue ? new Date(lockTimeValue) : null;

if(lockTime) {
    const lockDurationSeconds = 60;
    const now = new Date();

    let diff = Math.floor((lockDurationSeconds * 1000 - (now - lockTime)) / 1000);

    if(diff > 0) {
        document.getElementById('lock-message').style.display = 'block';
        document.getElementById('twoFactorForm').style.display = 'none';

        const countdownEl = document.getElementById('countdown');
        const interval = setInterval(() => {
            if(diff <= 0) {
                clearInterval(interval);
                document.getElementById('lock-message').style.display = 'none';
                document.getElementById('twoFactorForm').style.display = 'block';
                document.getElementById('unlock-message').style.display = 'block';  // 잠금 해제 메시지 보여주기

                // 10초 후 다시 숨기기
                setTimeout(() => {
                    document.getElementById('unlock-message').style.display = 'none';
                }, 10000);
            } else {
                countdownEl.textContent = diff;
                diff--;
            }
        }, 1000);
    }
}

const codeInput = document.getElementById('code');
const feedback = document.getElementById('code-feedback');
const submitBtn = document.getElementById('submitBtn');

if(codeInput && feedback && submitBtn) {
    codeInput.addEventListener('input', () => {
        const val = codeInput.value;

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
