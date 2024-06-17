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
                const threadsContainer = document.getElementById("threads");
                const threads = data.trim().split("\n");

                threads.forEach(thread => {
                    const [id, titre, userID, description] = thread.split(", ");
                    const threadElement = document.createElement("div");
                    threadElement.className = "thread";
                    threadElement.innerHTML = `
                        <h2>${titre}</h2>
                        <p>${description}</p>
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
window.onload = function() {
    const pathname = window.location.pathname;

    if (pathname === "/index.html" || pathname === "/") {
        index();
    } else if (pathname === "/forum_join.html") {
        forum_join();
    } else if (pathname === "/contact.html") {
        forum_join();
    }
};
