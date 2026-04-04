document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('jwt');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }
    const response = await fetch('/api/profile/me', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    if (response.ok) {
        const data = await response.json();
        renderProfile(data);
    } else if (response.status === 401) {
        logout();
    } else {
        console.error('Failed to load profile');
    }
});

function renderProfile(user) {
    const fields = {
        'profileUsernameH' : user.username,
        'profileUsername': user.username,
        'profileEmail': user.email,
        'profileRole': user.role,
        'profileStatus': user.status,
        'profileCreatedAt': new Date(user.createdAt).toLocaleString(),
        'profileLastLogin' : user.lastLogin,
        'profileLastIp' : user.lastIp,
        'profileSuccessfulLogin' : user.lastLogin
    };

    for (const [id, value] of Object.entries(fields)) {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = value;
        }
    }
}

function logout() {
    localStorage.removeItem('jwt');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    window.location.href = '/login.html';
}

const logoutBtn = document.getElementById('logoutBtnAllS');
if (logoutBtn) {
    logoutBtn.addEventListener('click', (e) => {
        e.preventDefault();
        logout();
    });
}