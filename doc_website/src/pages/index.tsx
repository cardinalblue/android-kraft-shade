import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import Heading from '@theme/Heading';
import Head from '@docusaurus/Head';

import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <Heading as="h1" className="hero__title">
          {siteConfig.title}
        </Heading>
        <p className="hero__subtitle">{siteConfig.tagline}</p>
        <div className={styles.buttons}>
          <Link
            className="button button--secondary button--lg"
            to="/docs/intro">
            Get Started with KraftShade
          </Link>
        </div>
      </div>
    </header>
  );
}

export default function Home(): ReactNode {
  const {siteConfig} = useDocusaurusContext();
  
  const jsonLdSchema = {
    "@context": "https://schema.org",
    "@type": "SoftwareApplication",
    "name": "KraftShade",
    "alternateName": "Kraft Shade",
    "description": "A modern, high-performance OpenGL ES graphics rendering library for Android, designed to provide a type-safe, Kotlin-first abstraction over OpenGL ES 2.0. Built with coroutines support and a focus on developer experience.",
    "url": "https://cardinalblue.github.io/android-kraft-shade/",
    "downloadUrl": "https://central.sonatype.com/artifact/com.cardinalblue/kraftshade",
    "applicationCategory": "DeveloperApplication",
    "applicationSubCategory": "Graphics Library",
    "operatingSystem": "Android",
    "programmingLanguage": "Kotlin",
    "runtimePlatform": "Android",
    "softwareVersion": "1.0",
    "dateCreated": "2025",
    "dateModified": new Date().toISOString().split('T')[0],
    "author": {
      "@type": "Organization",
      "name": "Cardinal Blue Software",
      "url": "https://cardinalblue.com"
    },
    "creator": {
      "@type": "Organization",
      "name": "Cardinal Blue Software",
      "url": "https://cardinalblue.com"
    },
    "publisher": {
      "@type": "Organization",
      "name": "Cardinal Blue Software",
      "url": "https://cardinalblue.com"
    },
    "maintainer": {
      "@type": "Organization",
      "name": "Cardinal Blue Software",
      "url": "https://cardinalblue.com"
    },
    "license": "https://www.apache.org/licenses/LICENSE-2.0",
    "codeRepository": "https://github.com/cardinalblue/android-kraft-shade",
    "programmingModel": "OpenGL ES",
    "targetProduct": {
      "@type": "SoftwareApplication",
      "name": "Android Applications",
      "operatingSystem": "Android"
    },
    "softwareRequirements": [
      "Android API 21+",
      "OpenGL ES 2.0+",
      "Kotlin 1.8+"
    ],
    "applicationSuite": "Android Graphics Processing",
    "keywords": [
      "kotlin",
      "android",
      "opengl-es",
      "image-processing",
      "graphics-library",
      "shaders",
      "jetpack-compose",
      "gpuimage-alternative",
      "android-graphics",
      "rendering-engine",
      "image-filters",
      "shader-effects"
    ],
    "featureList": [
      "Type-safe Kotlin-first design",
      "Coroutines support for async operations",
      "Flexible pipeline architecture",
      "Composable effects system",
      "Built-in shader library",
      "Jetpack Compose integration",
      "Automatic resource management",
      "Multi-pass rendering support",
      "Effect serialization",
      "Performance optimized"
    ],
    "screenshot": "https://cardinalblue.github.io/android-kraft-shade/img/docusaurus-social-card.jpg",
    "softwareHelp": {
      "@type": "CreativeWork",
      "name": "KraftShade Documentation",
      "url": "https://cardinalblue.github.io/android-kraft-shade/docs/intro"
    },
    "discussionUrl": "https://github.com/cardinalblue/android-kraft-shade/discussions",
    "releaseNotes": "https://github.com/cardinalblue/android-kraft-shade/releases",
    "installUrl": "https://central.sonatype.com/artifact/com.cardinalblue/kraftshade",
    "memoryRequirements": "Varies with usage",
    "processorRequirements": "ARM or x86 Android devices",
    "storageRequirements": "Minimal - library size ~500KB",
    "offers": {
      "@type": "Offer",
      "price": "0",
      "priceCurrency": "USD",
      "availability": "https://schema.org/InStock",
      "seller": {
        "@type": "Organization",
        "name": "Cardinal Blue Software"
      }
    }
  };

  return (
    <Layout
      title={`${siteConfig.title} - Android OpenGL Shader Pipeline`}
      description="KraftShade is a modern, high-performance OpenGL ES graphics rendering library for Android, designed to provide a type-safe, Kotlin-first abstraction over OpenGL ES 2.0.">
      <Head>
        <script type="application/ld+json">
          {JSON.stringify(jsonLdSchema, null, 2)}
        </script>
      </Head>
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
