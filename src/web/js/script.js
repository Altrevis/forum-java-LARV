function index() {
    document.getElementById('loginForm').addEventListener('submit', function(event) {
        event.preventDefault();

        var login = document.getElementById('username').value;
        var xhr = new XMLHttpRequest();
        xhr.open('POST', '/save-user-id', true);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

        var params = 'userID=' + encodeURIComponent(login);

        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4 && xhr.status === 200) {
                console.log('UserID saved successfully:', login);
                localStorage.setItem('username', login); 
                window.location.href = 'forum.html';
            }
        };

        xhr.send(params);
    });
}

function forum_join() {
    fetch("/get-threads")
        .then(response => response.text())
        .then(data => {
            const threadsContainer = document.getElementById("threads-container");
            const threads = data.trim().split("\n");

            threads.forEach(thread => {
                const [id, titre, userID, description] = thread.split(", ");
                const threadElement = document.createElement("div");
                threadElement.className = "thread";
                threadElement.innerHTML = `
                    <h2><a href="post.html?id=${id}">${titre}</a></h2>
                    <p>Posted by: ${userID}</p>
                `;
                threadsContainer.appendChild(threadElement);
            });
        })
        .catch(error => {
            console.error("Error fetching threads:", error);
        });
}


function contact() {
document.getElementById('contact-form').addEventListener('submit', function(e) {
    e.preventDefault();
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const message = document.getElementById('message').value;
    document.getElementById('form-status').innerText = "Message envoyé. Merci de nous avoir contactés!";
    document.getElementById('contact-form').reset();
});
}

function forum_create() {
    
    document.getElementById('create-thread-form').addEventListener('submit', function(event) {
        event.preventDefault();

        var title = document.getElementById('title').value;
        var description = document.getElementById('description').value;
        var pseudo = localStorage.getItem('username'); 

        if (!pseudo) {
            console.error('No username found. Please log in first.');
            return;
        }

        var xhr = new XMLHttpRequest();
        xhr.open('POST', '/save-thread', true);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

        var params = 'title=' + encodeURIComponent(title) + '&pseudo=' + encodeURIComponent(pseudo) + '&description=' + encodeURIComponent(description);

        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    console.log('Thread saved successfully:', title);
                    window.location.href = 'forum_join.html';
                } else {
                    console.error('Error saving thread:', xhr.responseText);
                }
            }
        };

        xhr.send(params);
    });
}

function loadThread() {
    const urlParams = new URLSearchParams(window.location.search);
    const threadId = urlParams.get('id');

    fetch(`/get-thread?id=${encodeURIComponent(threadId)}`)
        .then(response => response.text())
        .then(data => {
            const [threadInfo, ...messages] = data.trim().split("\n");
            const [titre, userID, description] = threadInfo.split(",");
            document.getElementById('thread-title').innerText = titre;
            document.getElementById('thread-user').innerText = `Posted by: ${userID}`;
            document.getElementById('thread-description').innerText = description;

            const messagesContainer = document.getElementById('messages-container');
            messagesContainer.innerHTML = ""; // Clear previous messages
            messages.forEach(msg => {
                const [messageID, messageUserID, message, timestamp, likes, dislikes] = msg.split(",");
                const messageElement = document.createElement("div");
                messageElement.className = "message";
                messageElement.innerHTML = `
                    <p><strong>${messageUserID}</strong> (${timestamp}): ${message}</p>
                    <div class="reaction-buttons">
                        <button class="like-btn" data-message-id="${messageID}">👍 <span class="like-count">${likes || 0}</span></button>
                        <button class="dislike-btn" data-message-id="${messageID}">👎 <span class="dislike-count">${dislikes || 0}</span></button>
                    </div>
                `;
                messagesContainer.appendChild(messageElement);
            });

            // Attach event listeners to like and dislike buttons
            addLikeDislikeEventListeners();
           
        })
        .catch(error => {
            console.error("Error loading thread:", error);
        });
}

function addLikeDislikeEventListeners() {
    document.querySelectorAll('.like-btn').forEach(button => {
        button.addEventListener('click', () => {
            const messageId = button.getAttribute('data-message-id');
            fetch('/update-like', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `messageId=${encodeURIComponent(messageId)}&isLike=true`
            })
            .then(response => response.text())
            .then(data => {
                const likeCountElement = button.querySelector('.like-count');
                likeCountElement.innerText = parseInt(likeCountElement.innerText) + 1;
            })
            .catch(error => {
                console.error('Error updating like:', error);
            });
        });
    });

    document.querySelectorAll('.dislike-btn').forEach(button => {
        button.addEventListener('click', () => {
            const messageId = button.getAttribute('data-message-id');
            fetch('/update-like', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `messageId=${encodeURIComponent(messageId)}&isLike=false`
            })
            .then(response => response.text())
            .then(data => {
                const dislikeCountElement = button.querySelector('.dislike-count');
                dislikeCountElement.innerText = parseInt(dislikeCountElement.innerText) + 1;
            })
            .catch(error => {
                console.error('Error updating dislike:', error);
            });
        });
    });
}


function postMessage() {
    document.getElementById('post-message-form').addEventListener('submit', function(event) {
        event.preventDefault();

        const message = document.getElementById('message').value.trim();
        const pseudo = localStorage.getItem('username');
        const urlParams = new URLSearchParams(window.location.search);
        const threadId = urlParams.get('id');

        if (!pseudo) {
            console.error('No username found. Please log in first.');
            return;
        }

        const xhr = new XMLHttpRequest();
        xhr.open('POST', '/save-message', true);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

        const params = `threadID=${encodeURIComponent(threadId)}&userID=${encodeURIComponent(pseudo)}&message=${encodeURIComponent(message)}`;

        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    const messageId = xhr.responseText.trim();
                    console.log('Message posted successfully:', message);

                  
                    const messagesContainer = document.getElementById('messages-container');
                    const messageElement = document.createElement("div");
                    messageElement.className = "message";
                    messageElement.innerHTML = `
                        <p><strong>${pseudo}</strong>: ${message}</p>
                        <div class="reaction-buttons">
                            <button class="like-btn" data-message-id="${messageId}">👍 <span class="like-count">0</span></button>
                            <button class="dislike-btn" data-message-id="${messageId}">👎 <span class="dislike-count">0</span></button>
                        </div>
                    `;
                    messagesContainer.appendChild(messageElement);

                    
                    addLikeDislikeEventListeners();

              
                    document.getElementById('message').value = '';
                } else {
                    console.error('Error posting message:', xhr.responseText);
                }
            }
        };

        xhr.send(params);
    });
}


window.onload = function() {
    const pathname = window.location.pathname;

    if (pathname === "/index.html" || pathname === "/") {
        index();
    } else if (pathname === "/forum_join.html") {
        forum_join();
    } else if (pathname === "/forum_create.html") {
        forum_create();
    } else if (pathname === "/post.html") {
        loadThread();
        postMessage();
    } else if (pathname === "/contact.html") {
        forum_join();
    }
};

setTimeout(function() {
    fadeOutPortalVideo();
}, 14000);

function fadeOutPortalVideo() {
    var portalVideo = document.getElementById('portalVideo');
    portalVideo.style.opacity = 0;
    setTimeout(function() {
        showNavbarAndLoginForm();
    }, 1000);
}

function showNavbarAndLoginForm() {
    var loginContainer = document.getElementById('loginContainer');
    var navbar = document.querySelector('.navbar');
    loginContainer.classList.add('visible');
    setTimeout(function() {
        navbar.style.opacity = 1;
    }, 1000);
}
