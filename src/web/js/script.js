// Function for the index page, handles login form submission
function index() {
    document.getElementById('loginForm').addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent form from submitting the default way

        var login = document.getElementById('username').value; // Get the username
        var xhr = new XMLHttpRequest(); // Create a new XMLHttpRequest object
        xhr.open('POST', '/save-user-id', true); // Configure it: POST-request to '/save-user-id'
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded'); // Set the request header

        var params = 'userID=' + encodeURIComponent(login); // Prepare the parameters

        xhr.onreadystatechange = function() { // Define what to do when the response is ready
            if (xhr.readyState === 4 && xhr.status === 200) {
                console.log('UserID saved successfully:', login); // Log success
                localStorage.setItem('username', login); // Save the username locally
                window.location.href = 'forum_join.html'; // Redirect to the forum join page
            }
        };

        xhr.send(params); // Send the request with the parameters
    });
}

// Function for the forum join page, fetches and displays threads
function forum_join() {
    fetch("/get-threads")
        .then(response => response.text())
        .then(data => {
            const threadsContainer = document.getElementById("threads-container"); // Get the container for threads
            const threads = data.trim().split("\n"); // Split the data into threads

            threads.forEach(thread => { // For each thread
                const [id, titre, userID, description] = thread.split(", "); // Split thread details
                const threadElement = document.createElement("div"); // Create a new div for the thread
                threadElement.className = "thread"; // Set the class name
                threadElement.innerHTML = `
                    <h2><a href="post.html?id=${id}">${titre}</a></h2>
                    <p>Posted by: ${userID}</p>
                    ${userID === localStorage.getItem('username') ? `<button class="delete-thread-btn" data-thread-id="${id}">Delete Thread</button>` : ''}
                `;
                threadsContainer.appendChild(threadElement); // Add the thread element to the container
            });
            addDeleteThreadEventListeners(); // Add event listeners for deleting threads
        })
        .catch(error => {
            console.error("Error fetching threads:", error); // Log any errors
        });
}

// Function for the forum create page, handles new thread form submission
function forum_create() {
    document.getElementById('create-thread-form').addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent the default form submission

        var title = document.getElementById('title').value; // Get the title
        var description = document.getElementById('description').value; // Get the description
        var pseudo = localStorage.getItem('username'); // Get the username

        if (!pseudo) { // Check if the username is not found
            console.error('No username found. Please log in first.');
            return;
        }

        var xhr = new XMLHttpRequest(); // Create a new XMLHttpRequest object
        xhr.open('POST', '/save-thread', true); // Configure it: POST-request to '/save-thread'
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded'); // Set the request header

        var params = 'title=' + encodeURIComponent(title) + '&pseudo=' + encodeURIComponent(pseudo) + '&description=' + encodeURIComponent(description); // Prepare the parameters

        xhr.onreadystatechange = function() { // Define what to do when the response is ready
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    console.log('Thread saved successfully:', title); // Log success
                    window.location.href = 'forum_join.html'; // Redirect to the forum join page
                } else {
                    console.error('Error saving thread:', xhr.responseText); // Log any errors
                }
            }
        };

        xhr.send(params); // Send the request with the parameters
    });
}

// Function to load a specific thread's details and messages
function loadThread() {
    const urlParams = new URLSearchParams(window.location.search); // Get URL parameters
    const threadId = urlParams.get('id'); // Get the thread ID from the URL

    fetch(`/get-thread?id=${encodeURIComponent(threadId)}`)
        .then(response => response.text())
        .then(data => {
            const [threadInfo, ...messages] = data.trim().split("\n"); // Split data into thread info and messages
            const [titre, userID, description] = threadInfo.split(","); // Split thread info
            document.getElementById('thread-title').innerText = titre; // Set the thread title
            document.getElementById('thread-user').innerText = `Posted by: ${userID}`; // Set the thread user
            document.getElementById('thread-description').innerText = description; // Set the thread description

            const messagesContainer = document.getElementById('messages-container'); // Get the messages container
            messagesContainer.innerHTML = ""; // Clear previous messages
            messages.forEach(msg => { // For each message
                const [messageID, messageUserID, message, timestamp, likes, dislikes] = msg.split(","); // Split message details
                const messageElement = document.createElement("div"); // Create a new div for the message
                messageElement.className = "message"; // Set the class name
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
                messagesContainer.appendChild(messageElement); // Add the message element to the container
            });

            // Attach event listeners to like and dislike buttons
            addLikeDislikeEventListeners();
            addDeleteMessageEventListeners();

            // Attach event listeners to new message-user elements
            document.querySelectorAll('.message-user').forEach(userElement => {
                userElement.addEventListener('click', () => openChat(userElement.textContent)); // Add click event to chat with user
            });
        })
        .catch(error => {
            console.error("Error loading thread:", error); // Log any errors
        });
        // Function to open chat with a user
    function openChat(user) {
        localStorage.setItem('chatWith', user); // Save the user to chat with
        window.location.href = 'private_conv.html'; // Redirect to the private conversation page
    }
}

// Function to handle like and dislike button events
function addLikeDislikeEventListeners() {
    document.querySelectorAll('.like-btn').forEach(button => { // For each like button
        button.addEventListener('click', () => {
            const messageId = button.getAttribute('data-message-id'); // Get the message ID
            const userID = localStorage.getItem('username'); // Get the username
            fetch('/update-like', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `messageId=${encodeURIComponent(messageId)}&isLike=true&userID=${encodeURIComponent(userID)}` // Send like update
            })
            .then(response => {
                if (response.ok) {
                    response.text();
                    loadThread(); // Reload the thread to update likes
                }
            })
            .catch(error => {
                console.error('Error updating like:', error); // Log any errors
            });
        });
    });

    document.querySelectorAll('.dislike-btn').forEach(button => { // For each dislike button
        button.addEventListener('click', () => {
            const messageId = button.getAttribute('data-message-id'); // Get the message ID
            const userID = localStorage.getItem('username'); // Get the username
            fetch('/update-like', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `messageId=${encodeURIComponent(messageId)}&isLike=false&userID=${encodeURIComponent(userID)}` // Send dislike update
            })
            .then(response => {
                if (response.ok) {
                    response.text();
                    loadThread(); // Reload the thread to update dislikes
                }
            })
            .catch(error => {
                console.error('Error updating dislike:', error); // Log any errors
            });
        });
    });
}

// Function to handle new message posting
function postMessage() {
    document.getElementById('post-message-form').addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent default form submission

        const message = document.getElementById('message').value.trim(); // Get the message text
        const pseudo = localStorage.getItem('username'); // Get the username
        const urlParams = new URLSearchParams(window.location.search); // Get URL parameters
        const threadId = urlParams.get('id'); // Get the thread ID

        if (!pseudo) { // Check if the username is not found
            console.error('No username found. Please log in first.');
            return;
        }

        const xhr = new XMLHttpRequest(); // Create a new XMLHttpRequest object
        xhr.open('POST', '/save-message', true); // Configure it: POST-request to '/save-message'
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded'); // Set the request header

        const params = `threadID=${encodeURIComponent(threadId)}&userID=${encodeURIComponent(pseudo)}&message=${encodeURIComponent(message)}`; // Prepare the parameters

        xhr.onreadystatechange = function() { // Define what to do when the response is ready
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    const messageId = xhr.responseText.trim(); // Get the message ID
                    console.log('Message posted successfully:', message);

                    const messagesContainer = document.getElementById('messages-container'); // Get the messages container
                    const messageElement = document.createElement("div"); // Create a new div for the message
                    messageElement.className = "message"; // Set the class name
                    messageElement.innerHTML = `
                        <p><strong>${pseudo}</strong>: ${message}</p>
                    `;
                    messagesContainer.appendChild(messageElement); // Add the message element to the container

                    loadThread(); // Reload the thread to update messages

                    document.getElementById('message').value = ''; // Clear the message input
                } else {
                    console.error('Error posting message:', xhr.responseText); // Log any errors
                }
            }
        };

        xhr.send(params); // Send the request with the parameters
    });
}

// Function to add event listeners for deleting messages
function addDeleteMessageEventListeners() {
    document.querySelectorAll('.delete-btn').forEach(button => { // For each delete button
        button.addEventListener('click', () => {
            const messageId = button.getAttribute('data-message-id'); // Get the message ID
            fetch(`/delete-message`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `messageID=${encodeURIComponent(messageId)}` // Send delete message request
            })
            .then(response => {
                if (response.ok) {
                    button.closest('.message').remove(); // Remove the message from the DOM
                } else {
                    console.error('Error deleting message'); // Log any errors
                }
            })
            .catch(error => {
                console.error('Error deleting message:', error); // Log any errors
            });
        });
    });
}

// Function to add event listeners for deleting threads
function addDeleteThreadEventListeners() {
    document.querySelectorAll('.delete-thread-btn').forEach(button => { // For each delete thread button
        button.addEventListener('click', () => {
            const threadId = button.getAttribute('data-thread-id'); // Get the thread ID
            fetch(`/delete-thread?id=${encodeURIComponent(threadId)}`, {
                method: 'POST',
            })
            .then(response => {
                if (response.ok) {
                    window.location.href = 'forum_join.html'; // Redirect to the forum join page
                } else {
                    console.error('Failed to delete thread:', response.statusText); // Log any errors
                }
            })
            .catch(error => {
                console.error('Error deleting thread:', error); // Log any errors
            });
        });
    });
}

// Function for the private chat page, fetches and displays users
function private() {
    fetch('/get-users')
        .then(response => response.text())
        .then(data => {
            const users = data.trim().split("\n"); // Split data into users
            const userList = document.getElementById('user-list'); // Get the user list container
            users.forEach(user => { // For each user
                const userItem = document.createElement('li'); // Create a new list item for the user
                userItem.textContent = user; // Set the user name
                userItem.addEventListener('click', () => openChat(user)); // Add click event to open chat
                userList.appendChild(userItem); // Add the user item to the list
            });
        })
        .catch(error => console.error("Error fetching users:", error)); // Log any errors
    
    // Function to open chat with a user
    function openChat(user) {
        localStorage.setItem('chatWith', user); // Save the user to chat with
        window.location.href = 'private_conv.html'; // Redirect to the private conversation page
    }
}

// Function for the private conversation page, fetches and displays messages
function privateConv() {
    const chatWith = localStorage.getItem('chatWith'); // Get the user to chat with
    document.getElementById('chat-with-user').textContent = chatWith; // Set the chat user name

    const fetchMessages = () => { // Function to fetch messages
        fetch(`/get-messages?user1=${encodeURIComponent(localStorage.getItem('username'))}&user2=${encodeURIComponent(chatWith)}`)
            .then(response => response.text())
            .then(data => {
                const messages = data.trim().split("\n"); // Split data into messages
                const chatMessages = document.getElementById('chat-messages'); // Get the chat messages container
                chatMessages.innerHTML = ""; // Clear previous messages
                messages.forEach(msg => { // For each message
                    const messageElement = document.createElement('div'); // Create a new div for the message
                    messageElement.className = 'message'; // Set the class name
                    messageElement.textContent = msg; // Set the message text
                    chatMessages.appendChild(messageElement); // Add the message element to the container
                });
            })
            .catch(error => console.error("Error fetching messages:", error)); // Log any errors
    };

    document.getElementById('chat-form').addEventListener('submit', event => { // Event listener for the chat form
        event.preventDefault(); // Prevent default form submission
        const message = document.getElementById('chat-message').value.trim(); // Get the message text
        const fromUser = localStorage.getItem('username'); // Get the username

        const xhr = new XMLHttpRequest(); // Create a new XMLHttpRequest object
        xhr.open('POST', '/save-chatmessage', true); // Configure it: POST-request to '/save-chatmessage'
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded'); // Set the request header

        const params = `fromUser=${encodeURIComponent(fromUser)}&toUser=${encodeURIComponent(chatWith)}&message=${encodeURIComponent(message)}`; // Prepare the parameters

        xhr.onreadystatechange = function() { // Define what to do when the response is ready
            if (xhr.readyState === 4 && xhr.status === 200) {
                console.log('Message sent successfully:', message);
                fetchMessages(); // Fetch messages to update chat
                document.getElementById('chat-message').value = ''; // Clear the chat input
            } else {
                console.error('Error sending message:', xhr.responseText); // Log any errors
            }
        };

        xhr.send(params); // Send the request with the parameters
    });
    setInterval(fetchMessages, 500); // Set interval to fetch messages every 500ms
    fetchMessages(); // Initial fetch of messages
}

// Function to run when the window loads
window.onload = function() {
    const pathname = window.location.pathname; // Get the current pathname

    // Route to the appropriate function based on the pathname
    if (pathname === "/index.html" || pathname === "/") {
        index();
    } else if (pathname === "/forum_join.html") {
        forum_join();
    } else if (pathname === "/forum_create.html") {
        forum_create();
    } else if (pathname === "/post.html") {
        loadThread();
        setInterval(loadThread, 1000); // Refresh thread every 1000ms
        postMessage();
    } else if (pathname === "/private.html") {
        private();
    } else if (pathname === "/private_conv.html") {
        privateConv();
    }
};

// Function to fade out the portal video after 14 seconds
setTimeout(function() {
    fadeOutPortalVideo();
}, 14000);

// Function to fade out the portal video
function fadeOutPortalVideo() {
    var portalVideo = document.getElementById('portalVideo'); // Get the portal video element
    portalVideo.style.opacity = 0; // Set opacity to 0
    setTimeout(function() {
        showNavbarAndLoginForm(); // Show the navbar and login form after fading out
    }, 100);
}

// Function to show the navbar and login form
function showNavbarAndLoginForm() {
    var loginContainer = document.getElementById('loginContainer'); // Get the login container
    var navbar = document.querySelector('.navbar'); // Get the navbar
    loginContainer.classList.add('visible'); // Make the login container visible
    setTimeout(function() {
        navbar.style.opacity = 1; // Set opacity of the navbar to 1
    }, 100);
}
