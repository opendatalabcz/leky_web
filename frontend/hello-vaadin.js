import { LitElement, html } from 'lit';

class HelloVaadin extends LitElement {
    render() {
        return html`<h1>Hello Vaadin from LitElement!</h1>`;
    }
}

customElements.define('hello-vaadin', HelloVaadin);
