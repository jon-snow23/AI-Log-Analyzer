import { expect } from 'vitest';

globalThis.expect = expect;

await import('@testing-library/jest-dom');
