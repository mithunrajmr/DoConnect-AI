import React from 'react';

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error };
    }

    componentDidCatch(error, errorInfo) {
        console.error("Uncaught error intercepted by Error Boundary:", error, errorInfo);
    }

    render() {
        if (this.state.hasError) {
            return (
                <div style={{
                    minHeight: '100vh',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    backgroundColor: '#05050A',
                    color: '#f0f0f0',
                    fontFamily: 'Inter, sans-serif',
                    padding: '20px'
                }}>
                    <div style={{
                        maxWidth: '500px',
                        background: '#18181b',
                        padding: '40px',
                        borderRadius: '16px',
                        border: '1px solid rgba(255, 255, 255, 0.06)',
                        boxShadow: '0 12px 40px rgba(0, 0, 0, 0.5)'
                    }}>
                        <h2 style={{ color: '#f87171', marginTop: 0 }}>Something went wrong</h2>
                        <p style={{ color: '#888', lineHeight: 1.6 }}>
                            A critical error occurred in the UI layer. We've logged this internally.
                        </p>
                        <button 
                            onClick={() => window.location.href = '/'}
                            style={{
                                marginTop: '20px',
                                padding: '10px 20px',
                                background: '#7c6aff',
                                color: 'white',
                                border: 'none',
                                borderRadius: '9999px',
                                cursor: 'pointer',
                                fontWeight: 600
                            }}
                        >
                            Return to Home
                        </button>
                    </div>
                </div>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;
