import type { ComponentProps } from 'react';
import styles from './TextButton.module.css';

type TextButtonProps = ComponentProps<'button'>;

export default function TextButton({ children, className, type = 'button', ...props }: TextButtonProps) {
    return (
        <button
            {...props}
            type={type}
            className={[styles.button, className].filter(Boolean).join(' ')}
        >
            {children}
        </button>
    );
}
