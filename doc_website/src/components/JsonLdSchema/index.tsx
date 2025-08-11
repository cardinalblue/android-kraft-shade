import React from 'react';
import Head from '@docusaurus/Head';

interface TechArticleSchemaProps {
  title: string;
  description: string;
  url: string;
  datePublished?: string;
  dateModified?: string;
  articleSection?: string;
}

export function TechArticleSchema({
  title,
  description,
  url,
  datePublished = '2025-01-01',
  dateModified = new Date().toISOString().split('T')[0],
  articleSection = 'Documentation'
}: TechArticleSchemaProps) {
  const schema = {
    "@context": "https://schema.org",
    "@type": "TechArticle",
    "headline": title,
    "description": description,
    "url": url,
    "datePublished": datePublished,
    "dateModified": dateModified,
    "articleSection": articleSection,
    "author": {
      "@type": "Organization",
      "name": "Cardinal Blue Software",
      "url": "https://cardinalblue.com"
    },
    "publisher": {
      "@type": "Organization",
      "name": "Cardinal Blue Software",
      "url": "https://cardinalblue.com",
      "logo": {
        "@type": "ImageObject",
        "url": "https://cardinalblue.github.io/android-kraft-shade/img/logo.svg"
      }
    },
    "mainEntityOfPage": {
      "@type": "WebPage",
      "@id": url
    },
    "about": {
      "@type": "SoftwareApplication",
      "name": "KraftShade",
      "description": "Android OpenGL ES graphics rendering library"
    },
    "genre": "Technology",
    "keywords": [
      "kotlin",
      "android",
      "opengl",
      "graphics",
      "documentation",
      "kraftshade"
    ],
    "educationalLevel": "Intermediate",
    "learningResourceType": "Documentation",
    "inLanguage": "en"
  };

  return (
    <Head>
      <script type="application/ld+json">
        {JSON.stringify(schema, null, 2)}
      </script>
    </Head>
  );
}

interface HowToSchemaProps {
  name: string;
  description: string;
  url: string;
  steps: string[];
  totalTime?: string;
  supply?: string[];
}

export function HowToSchema({
  name,
  description,
  url,
  steps,
  totalTime = "PT10M",
  supply = []
}: HowToSchemaProps) {
  const schema = {
    "@context": "https://schema.org",
    "@type": "HowTo",
    "name": name,
    "description": description,
    "url": url,
    "totalTime": totalTime,
    "supply": supply.map(item => ({
      "@type": "HowToSupply",
      "name": item
    })),
    "step": steps.map((step, index) => ({
      "@type": "HowToStep",
      "position": index + 1,
      "name": `Step ${index + 1}`,
      "text": step
    })),
    "author": {
      "@type": "Organization",
      "name": "Cardinal Blue Software"
    },
    "datePublished": "2025-01-01",
    "dateModified": new Date().toISOString().split('T')[0],
    "inLanguage": "en",
    "about": {
      "@type": "SoftwareApplication",
      "name": "KraftShade"
    }
  };

  return (
    <Head>
      <script type="application/ld+json">
        {JSON.stringify(schema, null, 2)}
      </script>
    </Head>
  );
}

export function APIReferenceSchema({
  name,
  description,
  url
}: {
  name: string;
  description: string;
  url: string;
}) {
  const schema = {
    "@context": "https://schema.org",
    "@type": "APIReference",
    "name": name,
    "description": description,
    "url": url,
    "programmingModel": "Object Oriented",
    "runtimePlatform": "Android",
    "targetProduct": {
      "@type": "SoftwareApplication",
      "name": "KraftShade",
      "operatingSystem": "Android"
    },
    "author": {
      "@type": "Organization",
      "name": "Cardinal Blue Software"
    },
    "datePublished": "2025-01-01",
    "dateModified": new Date().toISOString().split('T')[0],
    "inLanguage": "en"
  };

  return (
    <Head>
      <script type="application/ld+json">
        {JSON.stringify(schema, null, 2)}
      </script>
    </Head>
  );
}