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
                window.location.href = 'forum_join.html';
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
                    ${userID === localStorage.getItem('username') ? `<button class="delete-thread-btn" data-thread-id="${id}">Delete Thread</button>` : ''}
                `;
                threadsContainer.appendChild(threadElement);
            });
            addDeleteThreadEventListeners();
        })
        .catch(error => {
            console.error("Error fetching threads:", error);
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
                    <div class="message-header">
                        <div class="message-info">
                             <div class="message-user" title="Click to chat with ${messageUserID}">${messageUserID}</div>
                            <div class="message-time">${timestamp}</div>
                        </div>
                        <div class="reaction-buttons">
                            <button class="like-btn" data-message-id="${messageID}">👍 <span class="like-count">${likes || 0}</span></button>
                            <button class="dislike-btn" data-message-id="${messageID}">👎 <span class="dislike-count">${dislikes || 0}</span></button>
                            ${messageUserID === localStorage.getItem('username') ? `<button class="delete-btn" data-message-id="${messageID}">🗑️</button>` : ''}
                        </div>
                    </div>
                    <div class="message-text">${message}</div>
                `;
                messagesContainer.appendChild(messageElement);
            });

            // Attach event listeners to like and dislike buttons
            addLikeDislikeEventListeners();
            addDeleteMessageEventListeners();
        
        // Attach event listeners to new message-user elements
        document.querySelectorAll('.message-user').forEach(userElement => {
            userElement.addEventListener('click', () => openChat(userElement.textContent));
        });
        })
        .catch(error => {
            console.error("Error loading thread:", error);
        });
        function openChat(user) {
            localStorage.setItem('chatWith', user);
            window.location.href = 'private_conv.html';
        }
}


function addLikeDislikeEventListeners() {
    document.querySelectorAll('.like-btn').forEach(button => {
        button.addEventListener('click', () => {
            const messageId = button.getAttribute('data-message-id');
            const userID = localStorage.getItem('username');
            fetch('/update-like', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `messageId=${encodeURIComponent(messageId)}&isLike=true&userID=${encodeURIComponent(userID)}`
            })
            .then(response => {
                if (response.ok) {
                    response.text()
                    loadThread();
                }
            })
            .catch(error => {
                console.error('Error updating like:', error);
            });
        });
    });

    document.querySelectorAll('.dislike-btn').forEach(button => {
        button.addEventListener('click', () => {
            const messageId = button.getAttribute('data-message-id');
            const userID = localStorage.getItem('username');
            fetch('/update-like', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `messageId=${encodeURIComponent(messageId)}&isLike=false&userID=${encodeURIComponent(userID)}`
            })
            .then(response => {
                if (response.ok) {
                    response.text()
                    loadThread();
                }
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
                    `;
                    messagesContainer.appendChild(messageElement);

                    
                    loadThread();

              
                    document.getElementById('message').value = '';
                } else {
                    console.error('Error posting message:', xhr.responseText);
                }
            }
        };

        xhr.send(params);
    });
}

function addDeleteMessageEventListeners() {
    document.querySelectorAll('.delete-btn').forEach(button => {
        button.addEventListener('click', () => {
            const messageId = button.getAttribute('data-message-id');
            fetch(`/delete-message`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `messageID=${encodeURIComponent(messageId)}`
            })
            .then(response => {
                if (response.ok) {
                    button.closest('.message').remove();
                } else {
                    console.error('Error deleting message');
                }
            })
            .catch(error => {
                console.error('Error deleting message:', error);
            });
        });
    });
}

function addDeleteThreadEventListeners() {
    document.querySelectorAll('.delete-thread-btn').forEach(button => {
        button.addEventListener('click', () => {
            const threadId = button.getAttribute('data-thread-id');
            fetch(`/delete-thread?id=${encodeURIComponent(threadId)}`, {
                method: 'POST',
            })
            .then(response => {
                if (response.ok) {
                    
                    window.location.href = 'forum_join.html';
                } else {
                    console.error('Failed to delete thread:', response.statusText);
                }
            })
            .catch(error => {
                console.error('Error deleting thread:', error);
            });
        });
    });
}

function private() {
    fetch('/get-users')
        .then(response => response.text())
        .then(data => {
            const users = data.trim().split("\n");
            const userList = document.getElementById('user-list');
            users.forEach(user => {
                const userItem = document.createElement('li');
                userItem.textContent = user;
                userItem.addEventListener('click', () => openChat(user));
                userList.appendChild(userItem);
            });
        })
        .catch(error => console.error("Error fetching users:", error));
        function openChat(user) {
            localStorage.setItem('chatWith', user);
            window.location.href = 'private_conv.html';
        }
}



function privateConv() {
    const chatWith = localStorage.getItem('chatWith');
    document.getElementById('chat-with-user').textContent = chatWith;

    const fetchMessages = () => {
        fetch(`/get-messages?user1=${encodeURIComponent(localStorage.getItem('username'))}&user2=${encodeURIComponent(chatWith)}`)
            .then(response => response.text())
            .then(data => {
                const messages = data.trim().split("\n");
                const chatMessages = document.getElementById('chat-messages');
                chatMessages.innerHTML = ""; // Clear previous messages
                messages.forEach(msg => {
                    const messageElement = document.createElement('div');
                    messageElement.className = 'message';
                    messageElement.textContent = msg;
                    chatMessages.appendChild(messageElement);
                });
            })
            .catch(error => console.error("Error fetching messages:", error));
    };

    document.getElementById('chat-form').addEventListener('submit', event => {
        event.preventDefault();
        const message = document.getElementById('chat-message').value.trim();
        const fromUser = localStorage.getItem('username');

        const xhr = new XMLHttpRequest();
        xhr.open('POST', '/save-chatmessage', true);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

        const params = `fromUser=${encodeURIComponent(fromUser)}&toUser=${encodeURIComponent(chatWith)}&message=${encodeURIComponent(message)}`;

        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4 && xhr.status === 200) {
                console.log('Message sent successfully:', message);
                fetchMessages();
                document.getElementById('chat-message').value = '';
            } else {
                console.error('Error sending message:', xhr.responseText);
            }
        };

        xhr.send(params);
    });
    setInterval(fetchMessages, 500);
    fetchMessages();
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
        setInterval(loadThread, 950);
        postMessage();
    } else if (pathname === "/private.html") {
        private();
    } else if (pathname === "/private_conv.html") {
        privateConv();
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
    }, 100);
}

function showNavbarAndLoginForm() {
    var loginContainer = document.getElementById('loginContainer');
    var navbar = document.querySelector('.navbar');
    loginContainer.classList.add('visible');
    setTimeout(function() {
        navbar.style.opacity = 1;
    }, 100);
}

