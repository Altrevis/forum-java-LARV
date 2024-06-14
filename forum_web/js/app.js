document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const username = document.getElementById('username').value;
            if (username) {
                localStorage.setItem('username', username);
                window.location.href = 'forum.html';
            }
        });
    }

    const createThreadForm = document.getElementById('create-thread-form');
    if (createThreadForm) {
        createThreadForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const title = document.getElementById('title').value;
            const description = document.getElementById('description').value;
            // Logic to send thread data to server
            window.location.href = 'forum.html';
        });
    }

    const messageForm = document.getElementById('message-form');
    if (messageForm) {
        messageForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const message = document.getElementById('message').value;
            // Logic to send message data to server
            document.getElementById('messages').innerHTML += `<p>${localStorage.getItem('username')}: ${message}</p>`;
            document.getElementById('message').value = '';
        });
    }

    // Load threads and messages logic can be added here
});

document.getElementById('contact-form').addEventListener('submit', function(e) {
    e.preventDefault();
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const message = document.getElementById('message').value;
    document.getElementById('form-status').innerText = "Message envoyé. Merci de nous avoir contactés!";
    document.getElementById('contact-form').reset();
});
