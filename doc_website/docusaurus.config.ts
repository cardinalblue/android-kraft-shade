import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {
  title: 'Kraft Shade',
  tagline: 'Powerful Yet Simple: The Extensible Android OpenGL Shader Pipeline',
  favicon: 'img/favicon.ico',

  // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
  future: {
    v4: true, // Improve compatibility with the upcoming Docusaurus v4
  },

  // Set the production url of your site here
  url: 'https://cardinalblue.github.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/android-kraft-shade/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'cardinalblue', // Usually your GitHub org/user name.
  projectName: 'kraft-shade', // Usually your repo name.

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  markdown: {
    mermaid: true,
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl:
            'https://github.com/cardinalblue/android-kraft-shade/tree/main/doc_website',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  plugins: [
    '@docusaurus/theme-mermaid',
  ],

  headTags: [
    {
      tagName: 'script',
      attributes: {
        type: 'application/ld+json',
      },
      innerHTML: JSON.stringify({
        "@context": "https://schema.org",
        "@type": "Organization",
        "name": "Cardinal Blue Software",
        "url": "https://cardinalblue.com",
        "logo": "https://cardinalblue.github.io/android-kraft-shade/img/logo.svg",
        "sameAs": [
          "https://github.com/cardinalblue",
          "https://piccollage.com"
        ],
        "foundingDate": "2010",
        "description": "Cardinal Blue Software is a technology company specializing in creative mobile applications and developer tools.",
        "makesOffer": {
          "@type": "SoftwareApplication",
          "name": "KraftShade",
          "url": "https://cardinalblue.github.io/android-kraft-shade/"
        }
      }, null, 2),
    },
  ],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/docusaurus-social-card.jpg',
    colorMode: {
      defaultMode: 'dark',
      disableSwitch: true,
    },
    navbar: {
      title: 'Kraft Shade',
      logo: {
        alt: 'Kraft Shade Logo',
        src: 'img/logo.svg',
      },
      items: [
        {
          type: 'doc',
          docId: 'intro',
          position: 'left',
          label: 'Documentation',
        },
        {
          href: 'https://github.com/cardinalblue/android-kraft-shade',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
            title: 'Docs',
            items: [
              {
                label: 'Documentation',
                to: '/docs/intro',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/cardinalblue/android-kraft-shade',
              },
              {
                label: 'GitHub Discussions',
                href: 'https://github.com/cardinalblue/android-kraft-shade/discussions',
              },
              {
                label: 'Stack Overflow',
                href: 'https://stackoverflow.com/questions/tagged/kraftshade',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'PicCollage',
                href: 'https://piccollage.com/',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Cardinal Blue Software.`,
      },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java', 'toml'],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
