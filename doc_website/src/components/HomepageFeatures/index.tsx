import type {ReactNode} from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Modern Architecture',
    Svg: require('@site/static/img/undraw_mountain.svg').default,
    description: (
      <>
        Kotlin-first design with coroutines support, easy to use DSL for pipeline construction,
        type-safe builder pattern, and automatic resource cleanup.
      </>
    ),
  },
  {
    title: 'Flexible Pipeline',
    Svg: require('@site/static/img/undraw_tree.svg').default,
    description: (
      <>
        Composable effects, support for complex multi-pass rendering, efficient texture and buffer management,
        on/off-screen rendering support, and automatic buffer management.
      </>
    ),
  },
  {
    title: 'Developer Experience',
    Svg: require('@site/static/img/undraw_react.svg').default,
    description: (
      <>
        Flexible View components & Compose integration, flexible input system for effect adjustment and re-rendering,
        debugging tools with named buffer references, and comprehensive error handling.
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
