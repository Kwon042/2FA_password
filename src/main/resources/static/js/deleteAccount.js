function deleteAccount() {
    if (!confirm("정말로 회원을 탈퇴하시겠습니까?")) return;

    fetch("/api/users/delete", {
      method: "DELETE"
    })
    .then(response => {
      if (response.ok) {
        alert("회원 탈퇴가 완료되었습니다.");
        window.location.href = "/";
      } else {
        throw new Error("탈퇴 실패");
      }
    })
    .catch(error => {
      console.error("탈퇴 오류:", error);
      alert("탈퇴 중 문제가 발생했습니다.");
    });
}