import { api } from './api.js';
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
        console.log("Кнопка нажата, форма отправляется!");
        e.preventDefault();
            
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const loginMessage = document.getElementById('loginMessage');

            try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });
        
        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('jwt', data.token);
            localStorage.setItem('username', data.username);
            localStorage.setItem('role', data.role);
            window.location.href = '/dashboard.html';
        } else {
            showMessage(loginMessage, 'Incorrect email or password!', 'error');
        }
    } catch (error) {
    showMessage(loginMessage, 'server error', error);
    console.error('Login error:', error);
    }
        });
    } else {
    console.error("form id='loginForm' not found");
    }
    const showMessage = (element, text, type) => {
    if (!element) return;
    
    element.textContent = text;
    element.className = `message ${type} show`;

    setTimeout(() => {
        element.classList.remove('show');
    }, 5000);
}
const forgotForm = document.getElementById('forgotPasswordForm');
const showForgotBtn = document.getElementById('showForgotPassword');
const backToLoginBtn = document.getElementById('backToLogin');
showForgotBtn.addEventListener('click', (e) => {
    e.preventDefault();
    loginForm.classList.add('form-hidden-left');
    forgotForm.classList.add('form-show-center');
});
backToLoginBtn.addEventListener('click', (e) => { //back
    e.preventDefault();
    loginForm.classList.remove('form-hidden-left');
    forgotForm.classList.remove('form-show-center');
});
});