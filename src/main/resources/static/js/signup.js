document.getElementById('signup-form').addEventListener('submit', function(event) {
    event.preventDefault();

    const formData = {
        username: document.getElementById('username').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value
    };

    fetch("/api/users/signup", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(formData)
    })
    .then(response => {
        if (response.ok && response.headers.get("content-type")?.includes("application/json")) {
            return response.json();
        } else {
            throw new Error("Unexpected response type or error");
        }
    })
    .then(data => {
        alert("회원가입 성공!");
        window.location.href = "/login";
    })
    .catch(error => {
        console.error("회원가입 오류:", error);
        alert("회원가입 중 오류 발생");
    });

});