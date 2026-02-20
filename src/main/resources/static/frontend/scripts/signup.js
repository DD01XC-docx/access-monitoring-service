import {api} from './api.js';

document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    if (!registerForm) return;

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const messageDiv = document.getElementById('registerMessage');

        try {
            const response = await fetch ('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type' : 'application/json'},
                body: JSON.stringify({username, email, password}) //fix
            });
            if (response.ok) {
                alert('Reg successful');
                window.location.href='/login.html';
            } else {
                const errorText = await response.text();
                showMessage(messageDiv, errorText, 'error');
            }
        } catch (error) {
            showMessage(messageDiv, 'server error', 'error');
        }
    })
})
//show
function showMessage(element, text, type) {
    if (!element) return;
    element.textContent = text;
    element.className = `message ${type}`;
    element.style.display = 'block';
}