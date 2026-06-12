/**
 * @file index.js
 * @description The main entry point for the Social Media Aggregator Dashboard React application.
 * This file handles the initial rendering of the root component into the DOM.
 *
 * @project Social Media Aggregator Dashboard
 * @version 1.0.0
 * @author Senior Developer
 */

import React from 'react';
import ReactDOM from 'react-dom/client';

// Import global styles. It's crucial to import this before any components
// to ensure that base styles are applied first.
import './index.css';

// Import the main application components.
import DashboardGrid from './components/DashboardGrid';

// Optional: For measuring performance.
// import reportWebVitals from './reportWebVitals';

/**
 * The root component of the application.
 * It sets up the main layout, including a header, the main content area, and a footer.
 *
 * @returns {JSX.Element} The rendered App component.
 */
function App() {
  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Social Media Aggregator</h1>
        <p>Your unified view of the social world.</p>
      </header>
      <main className="app-main">
        <DashboardGrid />
      </main>
      <footer className="app-footer">
        <p>&copy; {new Date().getFullYear()} Social Media Aggregator Inc. All rights reserved.</p>
      </footer>
    </div>
  );
}

/**
 * Finds the root DOM node and initializes the React application render.
 * Throws an error if the root element is not found, preventing the app from running in a broken state.
 */
const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error("Fatal Error: The root element with id 'root' was not found in the DOM. The application cannot be mounted.");
}

const root = ReactDOM.createRoot(rootElement);

// Render the application.
// React.StrictMode is a wrapper that helps identify potential problems in an application.
// It activates additional checks and warnings for its descendants.
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

// If you want to start measuring performance in your app, you can pass a function
// to log results (for example: reportWebVitals(console.log)) or send to an
// analytics endpoint. This is useful for production monitoring.
// Learn more: https://bit.ly/CRA-vitals
// reportWebVitals(console.log);